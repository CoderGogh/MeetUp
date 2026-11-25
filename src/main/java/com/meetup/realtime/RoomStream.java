package com.meetup.realtime;

import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

/** SSE 연결 하나. 이벤트 ID = roomRev */
public final class RoomStream {

    private final String code;
    private final String participantId;
    private final SseEmitter emitter;

    RoomStream(String code, String participantId, SseEmitter emitter) {
        this.code = code;
        this.participantId = participantId;
        this.emitter = emitter;
    }

    public String code() {
        return code;
    }

    public String participantId() {
        return participantId;
    }

    public SseEmitter emitter() {
        return emitter;
    }
}
