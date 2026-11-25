package com.meetup.realtime;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.meetup.room.RoomEvent;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

/** 방별 최근 이벤트 보관 (RFP 8.6.3). 점수 = roomRev */
@Component
public class RoomEventLog {

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
}
