package com.meetup.realtime;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

/** 방 이벤트 구독 API (RFP 8.6.1) */
@RestController
class RoomEventController {

    static final String PARTICIPANT_TOKEN = "X-Participant-Token";

    private final RoomEventService events;

    RoomEventController(RoomEventService events) {
        this.events = events;
    }

    @GetMapping("/rooms/{code}/events")
    SseEmitter subscribe(@PathVariable String code,
            @RequestHeader(value = PARTICIPANT_TOKEN, required = false) String token) {
        return events.subscribe(code, token);
    }
}
