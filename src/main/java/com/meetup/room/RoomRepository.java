package com.meetup.room;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.meetup.place.Theme;
import java.time.Duration;
import java.time.Instant;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.RedisScript;
import org.springframework.stereotype.Repository;

/** 방·참여자 Redis 저장소 */
@Repository
public class RoomRepository {

    private static final RedisScript<Long> CREATE =
            RedisScript.of(new ClassPathResource("redis/room_create.lua"), Long.class);

    @SuppressWarnings("rawtypes")
    private static final RedisScript<List> JOIN =
            RedisScript.of(new ClassPathResource("redis/room_join.lua"), List.class);

    @SuppressWarnings("rawtypes")
    private static final RedisScript<List> TRANSITION =
            RedisScript.of(new ClassPathResource("redis/room_transition.lua"), List.class);

    private final StringRedisTemplate redis;
    private final ObjectMapper json;

    public RoomRepository(StringRedisTemplate redis, ObjectMapper json) {
        this.redis = redis;
        this.json = json;
    }

    String write(Participant p) {
        try {
            return json.writeValueAsString(p);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException(e);
        }
    }

    private Participant read(Object stored) {
        try {
            return json.readValue(stored.toString(), Participant.class);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException(e);
        }
    }

    // 코드가 이미 있으면 false
    public boolean create(Room room, Participant host, String participantTokenHash, Duration keyTtl) {
        Long created = redis.execute(CREATE, RoomKeys.all(room.code()), String.valueOf(keyTtl.toSeconds()),
                room.theme().name(), String.valueOf(room.capacity()), room.hostTokenHash(),
                String.valueOf(room.createdAt().toEpochMilli()), String.valueOf(room.expiresAt().toEpochMilli()),
                host.id(), write(host), Nicknames.key(host.nickname()), participantTokenHash);
        return Long.valueOf(1).equals(created);
    }

    // 정원·별명 중복·단계 검사 후 등록 (원자)
    public JoinResult join(String code, Participant p, String tokenHash, Instant now) {
        List<?> raw = redis.execute(JOIN, RoomKeys.all(code), String.valueOf(now.toEpochMilli()), p.id(), write(p),
                Nicknames.key(p.nickname()), tokenHash);
        return JoinResult.of(raw);
    }

    // 기대 단계일 때만 바꾸고 roomRev 증가 (원자)
    public CasResult transition(String code, RoomState expected, RoomState next) {
        return CasResult.of(redis.execute(TRANSITION, List.of(RoomKeys.room(code)), expected.name(), next.name()));
    }

    // 투표·추첨이 1위를 정하면 세움 (5주차)
    public boolean leaderDecided(String code) {
        return "1".equals(redis.opsForHash().get(RoomKeys.room(code), "leaderDecided"));
    }

    public Optional<Room> find(String code) {
        Map<Object, Object> h = redis.opsForHash().entries(RoomKeys.room(code));
        if (h.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(new Room(code, Theme.valueOf((String) h.get("theme")),
                Integer.parseInt((String) h.get("capacity")), RoomState.valueOf((String) h.get("state")),
                Long.parseLong((String) h.get("rev")), Instant.ofEpochMilli(Long.parseLong((String) h.get("createdAt"))),
                Instant.ofEpochMilli(Long.parseLong((String) h.get("expiresAt"))), (String) h.get("hostTokenHash")));
    }

    // 입장 순. 같은 ms면 방장 먼저
    public List<Participant> participants(String code) {
        return redis.opsForHash().values(RoomKeys.participants(code)).stream().map(this::read)
                .sorted(Comparator.comparingLong(Participant::joinedAt).thenComparing(p -> !p.host())).toList();
    }

    public Optional<Participant> participant(String code, String id) {
        return Optional.ofNullable(redis.opsForHash().get(RoomKeys.participants(code), id)).map(this::read);
    }

    public Optional<String> participantIdByToken(String code, String tokenHash) {
        return Optional.ofNullable((String) redis.opsForHash().get(RoomKeys.tokens(code), tokenHash));
    }
}
