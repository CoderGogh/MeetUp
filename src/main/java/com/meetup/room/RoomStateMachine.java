package com.meetup.room;

import com.meetup.common.ApiException;
import com.meetup.common.ErrorCode;
import java.time.Clock;
import java.util.Map;
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

    // 방장 이벤트는 방장만, 시스템 이벤트는 시스템만
    private static void authorize(RoomTrigger trigger, Actor actor, Map<String, Object> where) {
        boolean allowed = trigger.hostOnly() ? actor == Actor.HOST : actor == Actor.SYSTEM;
        if (!allowed) {
            throw new ApiException(ErrorCode.FORBIDDEN,
                    trigger.hostOnly() ? "방장만 할 수 있습니다" : "직접 요청할 수 없는 전이입니다", where);
        }
    }
}
