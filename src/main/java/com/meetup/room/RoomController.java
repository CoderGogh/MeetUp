package com.meetup.room;

import com.meetup.common.Idempotency;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** 방 API (RFP 8.6.1) */
@RestController
@RequestMapping("/rooms")
class RoomController {

    static final String PARTICIPANT_TOKEN = "X-Participant-Token";

    private final RoomService rooms;
    private final Idempotency idempotency;
    private final RoomProperties props;

    RoomController(RoomService rooms, Idempotency idempotency, RoomProperties props) {
        this.rooms = rooms;
        this.idempotency = idempotency;
        this.props = props;
    }
}
