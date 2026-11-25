package com.meetup.realtime;

import com.meetup.room.RoomEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;


/** 재전송·실시간 이벤트 순서 */
class RoomStreamTest {

    private static final Pattern ID = Pattern.compile("id:(\\d+)");

    // 보낸 이벤트 ID만 기록
    static class Recorder extends SseEmitter {

        final List<Long> ids = new ArrayList<>();

        @Override
        public void send(SseEventBuilder event) {
            String raw = event.build().stream().map(d -> String.valueOf(d.getData())).collect(Collectors.joining());
            Matcher m = ID.matcher(raw);
            if (m.find()) {
                ids.add(Long.parseLong(m.group(1)));
            }
        }
    }

    static RoomEvent event(long rev) {
        return new RoomEvent("C", rev, "participant.joined", Map.of());
    }

    final Recorder out = new Recorder();
    final RoomStream stream = new RoomStream("C", "p", out);
}
