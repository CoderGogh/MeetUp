package com.meetup.room;

import com.meetup.common.Idempotency;
import org.springframework.web.bind.annotation.RestController;

/** 입장 API */
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
}
