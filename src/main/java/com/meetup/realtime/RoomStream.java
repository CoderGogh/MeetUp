package com.meetup.realtime;

import com.meetup.room.RoomEvent;
import java.io.IOException;
import org.springframework.http.MediaType;
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

    // 실패하면 false (끊긴 연결)
    private boolean send(SseEmitter.SseEventBuilder event) {
        try {
            emitter.send(event);
            return true;
        } catch (IOException | IllegalStateException e) {
            return false;
        }
    }

    boolean send(RoomEvent e) {
        return send(SseEmitter.event().id(String.valueOf(e.roomRev())).name(e.type())
                .data(e, MediaType.APPLICATION_JSON));
    }
}
