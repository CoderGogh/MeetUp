package com.meetup.room;

import com.fasterxml.jackson.databind.JsonNode;
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

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class RoomApiTest {

    static final String BODY = "{\"theme\":\"CASUAL\",\"capacity\":5,\"nickname\":\"민지\"}";

    @Autowired
    MockMvc mvc;
    @Autowired
    ObjectMapper json;
    @Autowired
    StringRedisTemplate redis;

    ResultActions create(String key, String body) throws Exception {
        return mvc.perform(post("/rooms").header("Idempotency-Key", key)
                .contentType(MediaType.APPLICATION_JSON).content(body));
    }

    JsonNode created() throws Exception {
        String res = create(UUID.randomUUID().toString(), BODY).andReturn().getResponse().getContentAsString();
        return json.readTree(res);
    }

    @Test
    void createsRoomWithHostTokens() throws Exception {
        create(UUID.randomUUID().toString(), BODY).andExpect(status().isCreated())
                .andExpect(header().exists("Location"))
                .andExpect(jsonPath("$.code").isString())
                .andExpect(jsonPath("$.state").value("COLLECTING"))
                .andExpect(jsonPath("$.roomRev").value(1))
                .andExpect(jsonPath("$.hostToken").isString())
                .andExpect(jsonPath("$.participantToken").isString());
    }

    @Test
    void replaysSameRoomForSameKey() throws Exception {
        String key = UUID.randomUUID().toString();
        String first = create(key, BODY).andReturn().getResponse().getContentAsString();
        create(key, BODY).andExpect(status().isCreated()).andExpect(header().string("Idempotent-Replayed", "true"))
                .andExpect(jsonPath("$.code").value(json.readTree(first).get("code").asText()));
    }

    @Test
    void requiresIdempotencyKey() throws Exception {
        mvc.perform(post("/rooms").contentType(MediaType.APPLICATION_JSON).content(BODY))
                .andExpect(status().isBadRequest()).andExpect(jsonPath("$.details.header").value("Idempotency-Key"));
    }
}
