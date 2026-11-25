package com.meetup.realtime;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.meetup.room.RoomEvent;
import com.meetup.room.RoomKeys;
import java.util.List;
import java.util.Set;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.RedisScript;
import org.springframework.stereotype.Component;

/** 방별 최근 이벤트 보관 (RFP 8.6.3). 점수 = roomRev */
@Component
public class RoomEventLog {

    private static final RedisScript<Long> APPEND =
            RedisScript.of(new ClassPathResource("redis/event_append.lua"), Long.class);

    private final StringRedisTemplate redis;
    private final ObjectMapper json;
    private final RealtimeProperties props;

    public RoomEventLog(StringRedisTemplate redis, ObjectMapper json, RealtimeProperties props) {
        this.redis = redis;
        this.json = json;
        this.props = props;
    }

    private String write(RoomEvent e) {
        try {
            return json.writeValueAsString(e);
        } catch (JsonProcessingException ex) {
            throw new IllegalStateException(ex);
        }
    }

    private RoomEvent read(String raw) {
        try {
            return json.readValue(raw, RoomEvent.class);
        } catch (JsonProcessingException ex) {
            throw new IllegalStateException(ex);
        }
    }

    // 방이 이미 사라졌으면 false
    public boolean append(RoomEvent e) {
        Long kept = redis.execute(APPEND, List.of(RoomKeys.events(e.code()), RoomKeys.room(e.code())),
                String.valueOf(e.roomRev()), write(e), String.valueOf(props.retainedEvents()));
        return Long.valueOf(1).equals(kept);
    }

    // rev가 after보다 큰 이벤트, rev 순
    public List<RoomEvent> after(String code, long after) {
        Set<String> raw = redis.opsForZSet().rangeByScore(RoomKeys.events(code), after + 1, Double.MAX_VALUE);
        return raw == null ? List.of() : raw.stream().map(this::read).toList();
    }

    // 바로 다음 rev부터 이어지지 않으면 보관 범위 밖 → 재조회
    public Replay since(String code, long after, long currentRev) {
        if (after == currentRev) {
            return Replay.none(currentRev);
        }
        List<RoomEvent> events = after < currentRev ? after(code, after) : List.of();
        if (events.isEmpty() || events.get(0).roomRev() != after + 1) {
            return Replay.resync(currentRev);
        }
        return new Replay(events, false, currentRev);
    }
}
