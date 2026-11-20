package com.meetup.room;

import com.meetup.common.Idempotency;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
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

    @PostMapping("/rooms/{code}/transitions")
    ResponseEntity<Object> fire(@PathVariable String code,
            @RequestHeader(value = Idempotency.HEADER, required = false) String key,
            @RequestHeader(value = HOST_TOKEN, required = false) String hostToken,
            @RequestHeader(value = RoomController.PARTICIPANT_TOKEN, required = false) String participantToken,
            @Valid @RequestBody TransitionRequest req) {
        return idempotency.execute("room:" + code, key, idempotency.fingerprint("POST transitions", code, req),
                rooms.remaining(code), () -> ResponseEntity.ok(
                        machine.fire(code, req.trigger(), rooms.actor(code, hostToken, participantToken))));
    }
}
