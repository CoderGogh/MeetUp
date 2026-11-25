package com.meetup.realtime;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class RoomEventApiTest {

    @Autowired
    MockMvc mvc;

    ResultActions subscribe(String code, String token) throws Exception {
        return mvc.perform(get("/rooms/" + code + "/events").accept(MediaType.TEXT_EVENT_STREAM)
                .header("X-Participant-Token", token == null ? "" : token));
    }
}
