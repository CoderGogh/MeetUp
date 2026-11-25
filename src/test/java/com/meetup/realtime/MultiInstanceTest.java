package com.meetup.realtime;

import com.meetup.room.ParticipantService;
import com.meetup.room.RoomService;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.test.context.ActiveProfiles;


/** NFR-11: 인스턴스 A에서 난 변경을 B에 붙은 참여자도 받음 (Redis Pub/Sub) */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = "meetup.realtime.broadcast=redis")
@ActiveProfiles("test")
class MultiInstanceTest {

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
}
