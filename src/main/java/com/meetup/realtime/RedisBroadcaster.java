package com.meetup.realtime;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.meetup.room.RoomEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

/** Redis Pub/Sub으로 전 인스턴스에 (NFR-11). 자기 인스턴스도 구독으로 받음 */
@Component
@ConditionalOnProperty(name = "meetup.realtime.broadcast", havingValue = "redis", matchIfMissing = true)
class RedisBroadcaster implements EventBroadcaster {

    static final String CHANNEL = "meetup:room-events";

    private static final Logger log = LoggerFactory.getLogger(RedisBroadcaster.class);

    private final StringRedisTemplate redis;
    private final ObjectMapper json;
    private final RoomStreams streams;

    RedisBroadcaster(StringRedisTemplate redis, ObjectMapper json, RoomStreams streams) {
        this.redis = redis;
        this.json = json;
        this.streams = streams;
    }

    // 발행 실패 시 이 인스턴스 구독자에게라도. 나머지는 재접속 때 재전송
    @Override
    public void publish(RoomEvent e) {
        try {
            redis.convertAndSend(CHANNEL, json.writeValueAsString(e));
        } catch (JsonProcessingException | RuntimeException ex) {
            log.warn("room.event.publish failed code={} rev={}", e.code(), e.roomRev(), ex);
            streams.deliver(e);
        }
    }
}
