package com.meetup.realtime;

import com.meetup.room.CreatedRoom;
import com.meetup.room.JoinRequest;
import com.meetup.room.ParticipantService;
import com.meetup.room.RoomService;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;

/** NFR-06: 상태 변경은 1초 이내 전원에게 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
class DeliveryLatencyTest {

    @LocalServerPort
    int port;
    @Autowired
    RoomService rooms;
    @Autowired
    ParticipantService participants;

    // 방장 + 팀원 n명의 참여자 토큰
    List<String> members(CreatedRoom r, int n) {
        List<String> tokens = new ArrayList<>(List.of(r.participantToken()));
        for (int i = 1; i <= n; i++) {
            tokens.add(participants.join(r.code(), new JoinRequest("팀원" + i)).participantToken());
        }
        return tokens;
    }

    // 전원 구독, ready까지 대기
    List<SseClient> connect(String code, List<String> tokens) throws Exception {
        List<SseClient> clients = new ArrayList<>();
        for (String t : tokens) {
            SseClient c = new SseClient(port, code, t);
            assertThat(c.await("event:ready", Duration.ofSeconds(5))).isNotNull();
            clients.add(c);
        }
        return clients;
    }
}
