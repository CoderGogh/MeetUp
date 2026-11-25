package com.meetup.realtime;

import com.meetup.common.ApiException;
import com.meetup.common.ErrorCode;
import com.meetup.room.RoomService;
import java.util.Map;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

/** 방 이벤트 구독 (참여자만) */
@Service
public class RoomEventService {

    static final String LAST_EVENT_ID = "Last-Event-ID";

    private final RoomService rooms;
    private final RoomStreams streams;

    public RoomEventService(RoomService rooms, RoomStreams streams) {
        this.rooms = rooms;
        this.streams = streams;
    }

    // 없으면 null, 숫자가 아니면 400
    static Long lastEventId(String raw) {
        if (raw == null || raw.isBlank()) {
            return null;
        }
        try {
            return Long.parseLong(raw.trim());
        } catch (NumberFormatException e) {
            throw new ApiException(ErrorCode.INVALID_REQUEST, "Last-Event-ID는 숫자입니다",
                    Map.of("header", LAST_EVENT_ID));
        }
    }

    // 등록 뒤에 roomRev를 읽어 그 사이 이벤트를 놓치지 않음
    public SseEmitter subscribe(String code, String participantToken) {
        rooms.load(code);
        String me = rooms.authenticate(code, participantToken);
        RoomStream s = streams.open(code, me);
        s.ready(rooms.load(code).rev());
        return s.emitter();
    }
}
