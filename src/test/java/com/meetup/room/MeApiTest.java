package com.meetup.room;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** 내 상태 조회 (FR-21) */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class MeApiTest {

    @Autowired
    MockMvc mvc;
    @Autowired
    RoomService rooms;
    @Autowired
    ParticipantService participants;

    @Test
    void returnsMyInputAndCurrentStage() throws Exception {
        String code = RoomFixture.room(rooms, 5).code();
        JoinedRoom me = participants.join(code, new JoinRequest("준호"));
        mvc.perform(get("/rooms/" + code + "/participants/me").header("X-Participant-Token", me.participantToken()))
                .andExpect(status().isOk()).andExpect(jsonPath("$.participantId").value(me.participantId()))
                .andExpect(jsonPath("$.nickname").value("준호")).andExpect(jsonPath("$.host").value(false))
                .andExpect(jsonPath("$.originSubmitted").value(false))
                .andExpect(jsonPath("$.state").value("COLLECTING")).andExpect(jsonPath("$.roomRev").value(2));
    }

    @Test
    void noTokenIsUnauthorized() throws Exception {
        String code = RoomFixture.room(rooms, 5).code();
        mvc.perform(get("/rooms/" + code + "/participants/me")).andExpect(status().isUnauthorized());
    }
}
