package com.meetup.realtime;

import com.meetup.place.Theme;
import com.meetup.room.Actor;
import com.meetup.room.CreateRoomRequest;
import com.meetup.room.CreatedRoom;
import com.meetup.room.JoinRequest;
import com.meetup.room.ParticipantService;
import com.meetup.room.RoomKeys;
import com.meetup.room.RoomService;
import com.meetup.room.RoomState;
import com.meetup.room.RoomStateMachine;
import com.meetup.room.RoomTrigger;
import com.meetup.room.RoomView;
import java.time.Duration;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;

/** NFR-07: 강제 단절 후 재접속하면 끊긴 동안의 변경을 받아 상태가 일치 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
class ReconnectTest {

    private static final Duration WAIT = Duration.ofSeconds(2);

    @LocalServerPort
    int port;
    @Autowired
    RoomService rooms;
    @Autowired
    ParticipantService participants;
    @Autowired
    RoomStateMachine machine;
    @Autowired
    StringRedisTemplate redis;

    // 구독 → 입장(rev 2) 수신 → 강제 단절
    void receiveThenDrop(CreatedRoom r) throws Exception {
        SseClient first = new SseClient(port, r.code(), r.participantToken());
        participants.join(r.code(), new JoinRequest("준호"));
        assertThat(first.await("id:2", WAIT)).isNotNull();
        first.close();
    }

    // 끊긴 동안 입장 1건(rev 3) + 투표 시작(rev 4)
    void changeWhileAway(String code) {
        participants.join(code, new JoinRequest("서연"));
        redis.opsForHash().put(RoomKeys.room(code), "state", RoomState.REVIEWING.name());
        machine.fire(code, RoomTrigger.START_VOTING, Actor.HOST);
    }

    @Test
    void missedEventsArriveAfterForcedDisconnect() throws Exception {
        CreatedRoom r = rooms.create(new CreateRoomRequest(Theme.CASUAL, 5, "방장"));
        receiveThenDrop(r);
        changeWhileAway(r.code());

        try (SseClient again = new SseClient(port, r.code(), r.participantToken(), "2")) {
            assertThat(again.await("id:3", WAIT)).isNotNull();
            assertThat(again.await("\"count\":3", WAIT)).isNotNull();
            assertThat(again.await("id:4", WAIT)).isNotNull();
            assertThat(again.await("\"to\":\"VOTING\"", WAIT)).isNotNull();
        }
        RoomView now = rooms.view(r.code(), r.participantToken());
        assertThat(now.roomRev()).isEqualTo(4);
        assertThat(now.state()).isEqualTo(RoomState.VOTING);
        assertThat(now.participants()).hasSize(3);
    }
}
