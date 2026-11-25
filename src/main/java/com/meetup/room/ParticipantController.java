package com.meetup.room;

import com.meetup.common.Idempotency;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

/** 입장·내 상태 API */
@RestController
class ParticipantController {

    private final ParticipantService participants;
    private final RoomService rooms;
    private final Idempotency idempotency;

    ParticipantController(ParticipantService participants, RoomService rooms, Idempotency idempotency) {
        this.participants = participants;
        this.rooms = rooms;
        this.idempotency = idempotency;
    }

    @PostMapping("/rooms/{code}/participants")
    ResponseEntity<Object> join(@PathVariable String code,
            @RequestHeader(value = Idempotency.HEADER, required = false) String key,
            @Valid @RequestBody JoinRequest req) {
        return idempotency.execute("room:" + code, key, idempotency.fingerprint("POST participants", code, req),
                rooms.remaining(code), () -> ResponseEntity.status(HttpStatus.CREATED).body(participants.join(code, req)));
    }

    @GetMapping("/rooms/{code}/participants/me")
    MeView me(@PathVariable String code,
            @RequestHeader(value = RoomController.PARTICIPANT_TOKEN, required = false) String token) {
        return rooms.me(code, token);
    }
}
