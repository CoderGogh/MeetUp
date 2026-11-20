package com.meetup.room;

import java.time.Clock;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

/** 방 상태 전이 단일 진입점. 단계를 바꾸는 코드는 여기 한 곳 (RFP 8.4.3) */
@Service
public class RoomStateMachine {

    private static final Logger log = LoggerFactory.getLogger(RoomStateMachine.class);

    private final RoomRepository rooms;
    private final TransitionGuard guard;
    private final ApplicationEventPublisher events;
    private final Clock clock;

    public RoomStateMachine(RoomRepository rooms, TransitionGuard guard, ApplicationEventPublisher events,
            Clock clock) {
        this.rooms = rooms;
        this.guard = guard;
        this.events = events;
        this.clock = clock;
    }
}
