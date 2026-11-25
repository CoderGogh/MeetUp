package com.meetup.realtime;

import com.meetup.room.RoomEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

/** 방 이벤트 → 보관·구독자 전파 */
@Component
class RoomEventRelay {

    private static final Logger log = LoggerFactory.getLogger(RoomEventRelay.class);

    private final RoomEventLog events;
    private final EventBroadcaster broadcaster;

    RoomEventRelay(RoomEventLog events, EventBroadcaster broadcaster) {
        this.events = events;
        this.broadcaster = broadcaster;
    }

    // 보관 실패해도 전파는 함. 재접속 때 재조회로 메움
    private void store(RoomEvent e) {
        try {
            events.append(e);
        } catch (RuntimeException ex) {
            log.warn("room.event.store failed code={} rev={}", e.code(), e.roomRev(), ex);
        }
    }

    @EventListener
    void on(RoomEvent e) {
        store(e);
        broadcaster.publish(e);
    }
}
