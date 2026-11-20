package com.meetup.room;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class ParticipantApiTest {

    @Autowired
    MockMvc mvc;
    @Autowired
    RoomService rooms;
    @Autowired
    ObjectMapper json;
    @Autowired
    StringRedisTemplate redis;

    ResultActions join(String code, String key, String nickname) throws Exception {
        return mvc.perform(post("/rooms/" + code + "/participants").header("Idempotency-Key", key)
                .contentType(MediaType.APPLICATION_JSON).content("{\"nickname\":\"" + nickname + "\"}"));
    }

    @Test
    void joinReturnsToken() throws Exception {
        String code = RoomFixture.room(rooms, 5).code();
        join(code, UUID.randomUUID().toString(), "준호").andExpect(status().isCreated())
                .andExpect(jsonPath("$.participantToken").isString()).andExpect(jsonPath("$.roomRev").value(2));
    }

    @Test
    void replayReturnsSameParticipant() throws Exception {
        String code = RoomFixture.room(rooms, 5).code();
        String key = UUID.randomUUID().toString();
        String first = join(code, key, "준호").andReturn().getResponse().getContentAsString();
        join(code, key, "준호").andExpect(status().isCreated())
                .andExpect(jsonPath("$.participantId").value(json.readTree(first).get("participantId").asText()));
        assertThat(redis.opsForHash().size(RoomKeys.participants(code))).isEqualTo(2);
    }

    @Test
    void duplicateNicknameIsConflict() throws Exception {
        String code = RoomFixture.room(rooms, 5).code();
        join(code, UUID.randomUUID().toString(), "방장").andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("NICKNAME_TAKEN"));
    }
}
