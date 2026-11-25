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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;

/** NFR-11: 인스턴스 A에서 난 변경을 B에 붙은 참여자도 받음 (Redis Pub/Sub) */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = "meetup.realtime.broadcast=redis")
@ActiveProfiles("test")
class MultiInstanceTest {

    private static final Logger log = LoggerFactory.getLogger(MultiInstanceTest.class);
    private static final Duration LIMIT = Duration.ofSeconds(1);

    static ConfigurableApplicationContext other;

    @LocalServerPort
    int port;
    @Autowired
    RoomService rooms;
    @Autowired
    ParticipantService participants;

    @BeforeAll
    static void startOther() {
        other = TwoInstances.start("redis");
    }

    @AfterAll
    static void stopOther() {
        other.close();
    }

    CreatedRoom room() {
        return rooms.create(new CreateRoomRequest(Theme.CASUAL, 5, "방장"));
    }

    @Test
    void changeOnOneInstanceReachesTheOther() throws Exception {
        CreatedRoom r = room();
        try (SseClient onA = new SseClient(port, r.code(), r.participantToken());
                SseClient onB = new SseClient(TwoInstances.port(other), r.code(), r.participantToken())) {
            assertThat(onB.await("event:ready", LIMIT)).isNotNull();
            long start = System.nanoTime();
            participants.join(r.code(), new JoinRequest("준호"));
            assertThat(onB.await("id:2", LIMIT)).isNotNull();
            log.info("cross.instance.latency ms={}", (System.nanoTime() - start) / 1_000_000);
            assertThat(onA.await("id:2", LIMIT)).isNotNull();
        }
    }

    @Test
    void sameInstanceReceivesOnce() throws Exception {
        CreatedRoom r = room();
        try (SseClient onA = new SseClient(port, r.code(), r.participantToken())) {
            participants.join(r.code(), new JoinRequest("준호"));
            assertThat(onA.await("id:2", LIMIT)).isNotNull();
            assertThat(onA.await("id:2", Duration.ofMillis(300))).isNull();
        }
    }
}
