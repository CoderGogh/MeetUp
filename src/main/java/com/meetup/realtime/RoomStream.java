package com.meetup.realtime;

import com.meetup.room.RoomEvent;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.http.MediaType;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

/** SSE 연결 하나. 이벤트 ID = roomRev */
public final class RoomStream {

    private final String code;
    private final String participantId;
    private final SseEmitter emitter;
    private final Set<Long> replayed = ConcurrentHashMap.newKeySet();

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

    // 재전송한 이벤트가 실시간으로 또 오면 건너뜀
    boolean send(RoomEvent e) {
        return replayed.remove(e.roomRev()) || write(e);
    }

    // 먼저 표시해 두고 보냄 (등록과 조회 사이에 온 실시간 이벤트와 중복 방지)
    boolean replay(List<RoomEvent> events) {
        events.forEach(e -> replayed.add(e.roomRev()));
        return events.stream().allMatch(this::write);
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
