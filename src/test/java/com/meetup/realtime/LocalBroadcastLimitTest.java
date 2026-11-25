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
}
