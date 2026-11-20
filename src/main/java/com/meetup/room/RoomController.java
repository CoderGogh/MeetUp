package com.meetup.room;

import com.meetup.common.Idempotency;
import jakarta.validation.Valid;
import java.net.URI;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
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

    // 방 코드가 생기기 전이라 요청 키 보존 기간은 방 TTL
    @PostMapping
    ResponseEntity<Object> create(@RequestHeader(value = Idempotency.HEADER, required = false) String key,
            @Valid @RequestBody CreateRoomRequest req) {
        return idempotency.execute("rooms", key, idempotency.fingerprint("POST /rooms", req), props.ttl(), () -> {
            CreatedRoom room = rooms.create(req);
            return ResponseEntity.created(URI.create("/rooms/" + room.code())).body(room);
        });
    }
}
