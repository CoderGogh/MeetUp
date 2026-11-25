package com.meetup.realtime;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

/** 이 인스턴스에 붙은 방별 SSE 연결 */
@Component
public class RoomStreams {

    private final Map<String, Set<RoomStream>> rooms = new ConcurrentHashMap<>();
    private final RealtimeProperties props;

    public RoomStreams(RealtimeProperties props) {
        this.props = props;
    }

    public RoomStream open(String code, String participantId) {
        RoomStream s = new RoomStream(code, participantId, new SseEmitter(props.emitterTimeout().toMillis()));
        rooms.computeIfAbsent(code, k -> ConcurrentHashMap.newKeySet()).add(s);
        return s;
    }
}
