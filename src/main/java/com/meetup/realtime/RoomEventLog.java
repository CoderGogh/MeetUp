package com.meetup.realtime;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.meetup.room.RoomEvent;
import com.meetup.room.RoomKeys;
import java.util.List;
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

    // 방이 이미 사라졌으면 false
    public boolean append(RoomEvent e) {
        Long kept = redis.execute(APPEND, List.of(RoomKeys.events(e.code()), RoomKeys.room(e.code())),
                String.valueOf(e.roomRev()), write(e), String.valueOf(props.retainedEvents()));
        return Long.valueOf(1).equals(kept);
    }
}
