package com.meetup.realtime;

import com.meetup.room.RoomService;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

/** 방 이벤트 구독 (참여자만) */
@Service
public class RoomEventService {

    private final RoomService rooms;
    private final RoomStreams streams;

    public RoomEventService(RoomService rooms, RoomStreams streams) {
        this.rooms = rooms;
        this.streams = streams;
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
