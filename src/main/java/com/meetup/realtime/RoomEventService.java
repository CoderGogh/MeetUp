package com.meetup.realtime;

import com.meetup.common.ApiException;
import com.meetup.common.ErrorCode;
import com.meetup.room.RoomService;
import java.util.Map;
import java.util.function.Supplier;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

/** 방 이벤트 구독 (참여자만). Last-Event-ID가 있으면 끊긴 동안의 이벤트부터 */
@Service
public class RoomEventService {

    static final String LAST_EVENT_ID = "Last-Event-ID";

    private final RoomService rooms;
    private final RoomStreams streams;
    private final RoomEventLog events;

    public RoomEventService(RoomService rooms, RoomStreams streams, RoomEventLog events) {
        this.rooms = rooms;
        this.streams = streams;
        this.events = events;
    }

    private static long number(String raw, Supplier<ApiException> invalid) {
        try {
            return Long.parseLong(raw.trim());
        } catch (NumberFormatException | NullPointerException e) {
            throw invalid.get();
        }
    }

    // 없으면 null, 숫자가 아니면 400
    static Long lastEventId(String raw) {
        if (raw == null || raw.isBlank()) {
            return null;
        }
        return number(raw, () -> new ApiException(ErrorCode.INVALID_REQUEST, "Last-Event-ID는 숫자입니다",
                Map.of("header", LAST_EVENT_ID)));
    }

    // 등록 뒤에 roomRev를 읽어 그 사이 이벤트를 놓치지 않음
    public SseEmitter subscribe(String code, String participantToken, String lastEventId) {
        rooms.load(code);
        String me = rooms.authenticate(code, participantToken);
        Long after = lastEventId(lastEventId);
        RoomStream s = streams.open(code, me);
        long rev = rooms.load(code).rev();
        if (after == null) {
            s.skipUpTo(rev);
        } else {
            s.skipUpTo(after);
            catchUp(s, events.since(code, after, rev));
        }
        s.ready(rev);
        s.start();
        return s.emitter();
    }

    // 재조회 안내면 그 rev까지는 보내지 않음
    private static void catchUp(RoomStream s, Replay r) {
        if (r.resync()) {
            s.resync(r.roomRev());
            s.skipUpTo(r.roomRev());
        } else {
            s.replay(r.events());
        }
    }
}
