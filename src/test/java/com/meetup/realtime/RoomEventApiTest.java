package com.meetup.realtime;

import com.meetup.place.Theme;
import com.meetup.room.CreateRoomRequest;
import com.meetup.room.CreatedRoom;
import com.meetup.room.RoomService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.request;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class RoomEventApiTest {

    @Autowired
    MockMvc mvc;
    @Autowired
    RoomService rooms;

    CreatedRoom room() {
        return rooms.create(new CreateRoomRequest(Theme.CASUAL, 5, "방장"));
    }

    ResultActions subscribe(String code, String token) throws Exception {
        return mvc.perform(get("/rooms/" + code + "/events").accept(MediaType.TEXT_EVENT_STREAM)
                .header("X-Participant-Token", token == null ? "" : token));
    }

    MockHttpServletResponse stream(CreatedRoom r) throws Exception {
        return subscribe(r.code(), r.participantToken()).andExpect(request().asyncStarted()).andReturn()
                .getResponse();
    }

    @Test
    void subscribeWithoutTokenIsUnauthorized() throws Exception {
        subscribe(room().code(), null).andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("UNAUTHORIZED"));
    }

    @Test
    void unknownRoomIsNotFound() throws Exception {
        subscribe("ABCDEFGHJKMN", "x").andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("ROOM_NOT_FOUND"));
    }

    @Test
    void subscribeSendsReadyWithRoomRev() throws Exception {
        CreatedRoom r = room();
        assertThat(stream(r).getContentAsString()).contains("event:ready").contains("\"roomRev\":1");
    }
}
