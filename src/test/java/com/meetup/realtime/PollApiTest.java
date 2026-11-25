package com.meetup.realtime;

import com.meetup.room.ParticipantService;
import com.meetup.room.RoomService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;


/** SSE 차단 시 폴링 (RFP 6.8) */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class PollApiTest {

    @Autowired
    MockMvc mvc;
    @Autowired
    RoomService rooms;
    @Autowired
    ParticipantService participants;
}
