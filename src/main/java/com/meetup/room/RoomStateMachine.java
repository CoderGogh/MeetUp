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

    // 조회와 CAS 사이에 다른 요청이 먼저 바꾸면 그 단계로 409
    private Transition apply(String code, RoomTrigger trigger, RoomState from, RoomState to) {
        CasResult cas = rooms.transition(code, from, to);
        if (cas.status() < 0) {
            throw new ApiException(ErrorCode.ROOM_NOT_FOUND);
        }
        if (!cas.applied()) {
            throw new ApiException(ErrorCode.INVALID_TRANSITION,
                    Map.of("currentState", cas.state(), "roomRev", cas.rev()));
        }
        events.publishEvent(new RoomEvent(code, cas.rev(), "state.changed",
                Map.of("from", from.name(), "to", to.name())));
        log.info("room.transition code={} trigger={} from={} to={} rev={}", code, trigger, from, to, cas.rev());
        return new Transition(from, to, cas.rev());
    }
}
