package com.meetup.realtime;

import com.meetup.place.Theme;
import com.meetup.room.CreateRoomRequest;
import com.meetup.room.CreatedRoom;
import com.meetup.room.JoinRequest;
import com.meetup.room.ParticipantService;
import com.meetup.room.RoomService;
import java.time.Duration;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;

/** 비교 기준: 인스턴스 안에서만 전파하면 다른 인스턴스 참여자는 못 받음 (RFP 8.7 보류 항목 2) */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = "meetup.realtime.broadcast=local")
@ActiveProfiles("test")
class LocalBroadcastLimitTest {

    static ConfigurableApplicationContext other;

    @LocalServerPort
    int port;
    @Autowired
    RoomService rooms;
    @Autowired
    ParticipantService participants;

    @BeforeAll
    static void startOther() {
        other = TwoInstances.start("local");
    }

    @AfterAll
    static void stopOther() {
        other.close();
    }

    @Test
    void otherInstanceMissesLocalBroadcast() throws Exception {
        CreatedRoom r = rooms.create(new CreateRoomRequest(Theme.CASUAL, 5, "방장"));
        try (SseClient onA = new SseClient(port, r.code(), r.participantToken());
                SseClient onB = new SseClient(TwoInstances.port(other), r.code(), r.participantToken())) {
            assertThat(onB.await("event:ready", Duration.ofSeconds(1))).isNotNull();
            participants.join(r.code(), new JoinRequest("준호"));
            assertThat(onA.await("id:2", Duration.ofSeconds(1))).isNotNull();
            assertThat(onB.await("id:2", Duration.ofSeconds(1))).isNull();
        }
    }
}
