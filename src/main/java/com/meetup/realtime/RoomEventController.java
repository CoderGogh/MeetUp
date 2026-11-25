package com.meetup.realtime;

import org.springframework.http.CacheControl;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

/** 방 이벤트 구독 API (RFP 8.6.1). since가 있으면 폴링 */
@RestController
class RoomEventController {

    static final String PARTICIPANT_TOKEN = "X-Participant-Token";

    private final RoomEventService events;

    RoomEventController(RoomEventService events) {
        this.events = events;
    }

    @GetMapping(path = "/rooms/{code}/events", params = "!since")
    SseEmitter subscribe(@PathVariable String code,
            @RequestHeader(value = PARTICIPANT_TOKEN, required = false) String token,
            @RequestHeader(value = RoomEventService.LAST_EVENT_ID, required = false) String lastEventId) {
        return events.subscribe(code, token, lastEventId);
    }

    // 폴링. 캐시하면 새 이벤트를 못 봄
    @GetMapping(path = "/rooms/{code}/events", params = "since", produces = MediaType.APPLICATION_JSON_VALUE)
    ResponseEntity<Replay> poll(@PathVariable String code,
            @RequestHeader(value = PARTICIPANT_TOKEN, required = false) String token,
            @RequestParam String since) {
        return ResponseEntity.ok().cacheControl(CacheControl.noStore()).body(events.poll(code, token, since));
    }
}
