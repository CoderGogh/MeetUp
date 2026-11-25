package com.meetup.realtime;

import com.meetup.place.Theme;
import com.meetup.room.CreateRoomRequest;
import com.meetup.room.CreatedRoom;
import com.meetup.room.JoinRequest;
import com.meetup.room.ParticipantService;
import com.meetup.room.RoomService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;

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

    // 방장 + 2명 입장 → rev 3
    CreatedRoom roomWithTwoJoins() {
        CreatedRoom r = rooms.create(new CreateRoomRequest(Theme.CASUAL, 5, "방장"));
        participants.join(r.code(), new JoinRequest("준호"));
        participants.join(r.code(), new JoinRequest("서연"));
        return r;
    }

    ResultActions poll(CreatedRoom r, String since) throws Exception {
        return mvc.perform(get("/rooms/" + r.code() + "/events").param("since", since)
                .header("X-Participant-Token", r.participantToken()));
    }
}
