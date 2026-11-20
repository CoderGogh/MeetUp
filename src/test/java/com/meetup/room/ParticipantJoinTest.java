package com.meetup.room;

import com.meetup.common.ApiException;
import com.meetup.common.ErrorCode;
import java.util.concurrent.Callable;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.IntStream;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.event.ApplicationEvents;
import org.springframework.test.context.event.RecordApplicationEvents;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
@RecordApplicationEvents
class ParticipantJoinTest {

    @Autowired
    RoomService rooms;
    @Autowired
    ParticipantService participants;
    @Autowired
    StringRedisTemplate redis;
    @Autowired
    ApplicationEvents events;

    JoinedRoom join(String code, String nickname) {
        return participants.join(code, new JoinRequest(nickname));
    }

    ErrorCode failure(Runnable action) {
        try {
            action.run();
            return null;
        } catch (ApiException e) {
            return e.code();
        }
    }

    // 동시 입장 n건 중 성공 수
    int concurrently(String code, int n, java.util.function.IntFunction<String> nickname) throws Exception {
        var ok = new AtomicInteger();
        var pool = Executors.newFixedThreadPool(10);
        pool.invokeAll(IntStream.range(0, n).<Callable<Void>>mapToObj(i -> () -> {
            if (failure(() -> join(code, nickname.apply(i))) == null) {
                ok.incrementAndGet();
            }
            return null;
        }).toList());
        pool.shutdown();
        return ok.get();
    }

    @Test
    void joinsAndAuthenticates() {
        String code = RoomFixture.room(rooms, 5).code();
        JoinedRoom me = join(code, "준호");
        assertThat(me.roomRev()).isEqualTo(2);
        RoomView view = rooms.view(code, me.participantToken());
        assertThat(view.me()).isEqualTo(me.participantId());
        assertThat(view.participants()).extracting(RoomView.Member::nickname).containsExactly("방장", "준호");
    }

    @Test
    void rejectsDuplicateNicknameIgnoringCase() {
        String code = RoomFixture.room(rooms, 5).code();
        join(code, "Jun");
        assertThat(failure(() -> join(code, "jun"))).isEqualTo(ErrorCode.NICKNAME_TAKEN);
    }

    @Test
    void rejectsWhenFull() {
        String code = RoomFixture.room(rooms, 2).code();
        join(code, "준호");
        assertThat(failure(() -> join(code, "서연"))).isEqualTo(ErrorCode.ROOM_FULL);
    }

    @Test
    void concurrentJoinsNeverExceedCapacity() throws Exception {
        String code = RoomFixture.room(rooms, 5).code();
        assertThat(concurrently(code, 20, i -> "참여자" + i)).isEqualTo(4);
        assertThat(redis.opsForHash().size(RoomKeys.participants(code))).isEqualTo(5);
    }

    @Test
    void concurrentSameNicknameOnlyOneWins() throws Exception {
        String code = RoomFixture.room(rooms, 10).code();
        assertThat(concurrently(code, 10, i -> "같은이름")).isEqualTo(1);
    }
}
