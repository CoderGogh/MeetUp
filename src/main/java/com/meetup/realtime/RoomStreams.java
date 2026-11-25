package com.meetup.realtime;

import com.meetup.room.RoomEvent;
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

    // 완료·시간 초과·오류 시 자동 정리
    public RoomStream open(String code, String participantId) {
        RoomStream s = new RoomStream(code, participantId, new SseEmitter(props.emitterTimeout().toMillis()));
        rooms.computeIfAbsent(code, k -> ConcurrentHashMap.newKeySet()).add(s);
        s.emitter().onCompletion(() -> remove(s));
        s.emitter().onTimeout(() -> remove(s));
        s.emitter().onError(e -> remove(s));
        return s;
    }

    // 마지막 연결이 빠지면 방 항목도 제거
    void remove(RoomStream s) {
        rooms.computeIfPresent(s.code(), (k, set) -> {
            set.remove(s);
            return set.isEmpty() ? null : set;
        });
    }

    // 방 구독자 전원에게. 보내지 못한 연결은 정리
    public void deliver(RoomEvent e) {
        for (RoomStream s : rooms.getOrDefault(e.code(), Set.of())) {
            if (!s.send(e)) {
                remove(s);
            }
        }
    }

    public int count(String code) {
        return rooms.getOrDefault(code, Set.of()).size();
    }
}
