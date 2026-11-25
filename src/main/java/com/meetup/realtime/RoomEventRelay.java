package com.meetup.realtime;

import com.meetup.room.RoomEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

/** 방 이벤트 → 구독자 전파 */
@Component
class RoomEventRelay {

    private final RoomStreams streams;

    RoomEventRelay(RoomStreams streams) {
        this.streams = streams;
    }

    @EventListener
    void on(RoomEvent e) {
        streams.deliver(e);
    }
}
