package com.meetup.realtime;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.meetup.room.RoomEvent;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

/** Redis Pub/Sub으로 전 인스턴스에 (NFR-11). 자기 인스턴스도 구독으로 받음 */
@Component
@ConditionalOnProperty(name = "meetup.realtime.broadcast", havingValue = "redis", matchIfMissing = true)
class RedisBroadcaster implements EventBroadcaster {

    static final String CHANNEL = "meetup:room-events";

    private final StringRedisTemplate redis;
    private final ObjectMapper json;

    RedisBroadcaster(StringRedisTemplate redis, ObjectMapper json) {
        this.redis = redis;
        this.json = json;
    }

    @Override
    public void publish(RoomEvent e) {
        try {
            redis.convertAndSend(CHANNEL, json.writeValueAsString(e));
        } catch (JsonProcessingException ex) {
            throw new IllegalStateException(ex);
        }
    }
}
