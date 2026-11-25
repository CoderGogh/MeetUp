package com.meetup.realtime;

import com.meetup.room.RoomEvent;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

/** 이 인스턴스 구독자에게만 (단일 인스턴스) */
@Component
@ConditionalOnProperty(name = "meetup.realtime.broadcast", havingValue = "local")
class LocalBroadcaster implements EventBroadcaster {

    private final RoomStreams streams;

    LocalBroadcaster(RoomStreams streams) {
        this.streams = streams;
    }

    @Override
    public void publish(RoomEvent e) {
        streams.deliver(e);
    }
}
