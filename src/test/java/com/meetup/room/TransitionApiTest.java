package com.meetup.room;

import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class TransitionApiTest {

    @Autowired
    MockMvc mvc;
    @Autowired
    RoomService rooms;
    @Autowired
    StringRedisTemplate redis;

    MockHttpServletRequestBuilder fire(String code, String key, String trigger) {
        return post("/rooms/" + code + "/transitions").header("Idempotency-Key", key)
                .contentType(MediaType.APPLICATION_JSON).content("{\"trigger\":\"" + trigger + "\"}");
    }

    @Test
    void hostStartsVoting() throws Exception {
        CreatedRoom room = RoomFixture.room(rooms, 5);
        RoomFixture.setState(redis, room.code(), RoomState.REVIEWING);
        String key = UUID.randomUUID().toString();
        mvc.perform(fire(room.code(), key, "START_VOTING").header("X-Host-Token", room.hostToken()))
                .andExpect(status().isOk()).andExpect(jsonPath("$.state").value("VOTING"))
                .andExpect(jsonPath("$.roomRev").value(2));
        mvc.perform(fire(room.code(), key, "START_VOTING").header("X-Host-Token", room.hostToken()))
                .andExpect(status().isOk()).andExpect(header().string("Idempotent-Replayed", "true"));
    }

    @Test
    void memberIsForbidden() throws Exception {
        CreatedRoom room = RoomFixture.room(rooms, 5);
        mvc.perform(fire(room.code(), UUID.randomUUID().toString(), "CALCULATE_CANDIDATES")
                        .header("X-Participant-Token", room.participantToken()))
                .andExpect(status().isForbidden()).andExpect(jsonPath("$.details.currentState").value("COLLECTING"));
    }
}
