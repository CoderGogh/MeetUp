package com.meetup.realtime;

import com.meetup.room.RoomEvent;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import org.springframework.http.MediaType;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

/** SSE 연결 하나. 이벤트 ID = roomRev */
public final class RoomStream {

    private final String code;
    private final String participantId;
    private final SseEmitter emitter;
    private final Object lock = new Object();
    private long sent;

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

    private boolean write(RoomEvent e) {
        return send(SseEmitter.event().id(String.valueOf(e.roomRev())).name(e.type())
                .data(e, MediaType.APPLICATION_JSON));
    }

    // 보낸 rev 이하는 건너뜀 (재전송과 겹친 실시간 이벤트, 클라이언트도 버릴 이벤트)
    private boolean emit(RoomEvent e) {
        if (e.roomRev() <= sent) {
            return true;
        }
        sent = e.roomRev();
        return write(e);
    }

    boolean send(RoomEvent e) {
        synchronized (lock) {
            return emit(e);
        }
    }

    boolean replay(List<RoomEvent> events) {
        synchronized (lock) {
            return events.stream().allMatch(this::emit);
        }
    }

    // 보관 범위 밖 재접속. 클라이언트는 GET /rooms/{code}로 다시 맞춤
    boolean resync(long roomRev) {
        return send(SseEmitter.event().name("resync").data(Map.of("roomRev", roomRev), MediaType.APPLICATION_JSON));
    }

    // 연결 직후 현재 roomRev. ID가 없어 Last-Event-ID를 덮지 않음
    boolean ready(long roomRev) {
        return send(SseEmitter.event().name("ready").data(Map.of("roomRev", roomRev), MediaType.APPLICATION_JSON));
    }

    // 프록시 유휴 끊김 방지·끊긴 연결 감지
    boolean ping() {
        return send(SseEmitter.event().comment("ping"));
    }
}
