package com.meetup.realtime;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.meetup.room.RoomEvent;
import java.io.IOException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;

/** Pub/Sub 메시지 → 이 인스턴스 구독자 */
class RedisEventSubscriber implements MessageListener {

    private static final Logger log = LoggerFactory.getLogger(RedisEventSubscriber.class);

    private final ObjectMapper json;
    private final RoomStreams streams;

    RedisEventSubscriber(ObjectMapper json, RoomStreams streams) {
        this.json = json;
        this.streams = streams;
    }

    // 깨진 메시지는 버림
    @Override
    public void onMessage(Message message, byte[] pattern) {
        try {
            streams.deliver(json.readValue(message.getBody(), RoomEvent.class));
        } catch (IOException e) {
            log.warn("room.event.subscribe unreadable message", e);
        }
    }
}
