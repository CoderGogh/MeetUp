package com.meetup.realtime;

import com.meetup.room.RoomEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Test;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import static org.assertj.core.api.Assertions.assertThat;

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

    @Test
    void liveEventDuringReplayComesAfterIt() {
        stream.send(event(5));
        stream.skipUpTo(2);
        stream.replay(List.of(event(3), event(4), event(5)));
        stream.start();
        assertThat(out.ids).containsExactly(3L, 4L, 5L);
    }

    @Test
    void heldEventsGoOutInRevOrder() {
        stream.send(event(7));
        stream.send(event(6));
        stream.skipUpTo(5);
        stream.start();
        assertThat(out.ids).containsExactly(6L, 7L);
    }

    @Test
    void staleLiveEventIsSkipped() {
        stream.skipUpTo(5);
        stream.send(event(4));
        stream.start();
        stream.send(event(6));
        stream.send(event(5));
        assertThat(out.ids).containsExactly(6L);
    }
}
