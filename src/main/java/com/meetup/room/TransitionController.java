package com.meetup.room;

import com.meetup.common.Idempotency;
import org.springframework.web.bind.annotation.RestController;

/** 단계 전환 API (방장). 후보지 산출·확정 API도 4~5주차에 같은 진입점을 씀 */
@RestController
class TransitionController {

    static final String HOST_TOKEN = "X-Host-Token";

    private final RoomStateMachine machine;
    private final RoomService rooms;
    private final Idempotency idempotency;

    TransitionController(RoomStateMachine machine, RoomService rooms, Idempotency idempotency) {
        this.machine = machine;
        this.rooms = rooms;
        this.idempotency = idempotency;
    }
}
