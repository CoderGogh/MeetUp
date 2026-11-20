package com.meetup.room;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.Instant;
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

    @Test
    void rejectsCapacityOverMax() throws Exception {
        create(UUID.randomUUID().toString(), BODY.replace("5", "11")).andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.details.fields.capacity").exists());
    }

    @Test
    void participantSeesRoom() throws Exception {
        JsonNode room = created();
        mvc.perform(get("/rooms/" + room.get("code").asText())
                        .header("X-Participant-Token", room.get("participantToken").asText()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.me").value(room.get("participantId").asText()))
                .andExpect(jsonPath("$.participants[0].nickname").value("민지"))
                .andExpect(jsonPath("$.participants[0].host").value(true))
                .andExpect(jsonPath("$.hostTokenHash").doesNotExist());
    }

    @Test
    void rejectsMissingOrWrongToken() throws Exception {
        String code = created().get("code").asText();
        mvc.perform(get("/rooms/" + code)).andExpect(status().isUnauthorized());
        mvc.perform(get("/rooms/" + code).header("X-Participant-Token", "wrong"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void unknownRoomIsNotFound() throws Exception {
        mvc.perform(get("/rooms/" + RoomCodes.generate()).header("X-Participant-Token", "x"))
                .andExpect(status().isNotFound()).andExpect(jsonPath("$.code").value("ROOM_NOT_FOUND"));
        mvc.perform(get("/rooms/bad-code")).andExpect(status().isNotFound());
    }

    @Test
    void expiredRoomIsGone() throws Exception {
        JsonNode room = created();
        String code = room.get("code").asText();
        redis.opsForHash().put(RoomKeys.room(code), "expiresAt", String.valueOf(Instant.now().minusSeconds(1).toEpochMilli()));
        mvc.perform(get("/rooms/" + code).header("X-Participant-Token", room.get("participantToken").asText()))
                .andExpect(status().isGone()).andExpect(jsonPath("$.code").value("ROOM_EXPIRED"));
    }

    @Test
    void keysExpireWithinTtlPlusGrace() throws Exception {
        String code = created().get("code").asText();
        for (String key : RoomKeys.all(code)) {
            assertThat(redis.getExpire(key)).isPositive().isLessThanOrEqualTo(7 * 3600);
        }
    }

    @Test
    void storesOnlyTokenHashes() throws Exception {
        JsonNode room = created();
        String code = room.get("code").asText();
        assertThat(redis.opsForHash().values(RoomKeys.room(code)).toString())
                .doesNotContain(room.get("hostToken").asText());
        assertThat(redis.opsForHash().keys(RoomKeys.tokens(code)).toString())
                .doesNotContain(room.get("participantToken").asText());
    }
}
