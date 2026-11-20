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

    // 입장 순
    public List<Participant> participants(String code) {
        return redis.opsForHash().values(RoomKeys.participants(code)).stream().map(this::read)
                .sorted(Comparator.comparingLong(Participant::joinedAt)).toList();
    }

    public Optional<String> participantIdByToken(String code, String tokenHash) {
        return Optional.ofNullable((String) redis.opsForHash().get(RoomKeys.tokens(code), tokenHash));
    }
}
