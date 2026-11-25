package com.meetup.realtime;

import com.meetup.place.Theme;
import com.meetup.room.Actor;
import com.meetup.room.CreateRoomRequest;
import com.meetup.room.CreatedRoom;
import com.meetup.room.JoinRequest;
import com.meetup.room.ParticipantService;
import com.meetup.room.RoomKeys;
import com.meetup.room.RoomService;
import com.meetup.room.RoomState;
import com.meetup.room.RoomStateMachine;
import com.meetup.room.RoomTrigger;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.StringRedisTemplate;
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
    @Autowired
    ParticipantService participants;
    @Autowired
    RoomStateMachine machine;
    @Autowired
    StringRedisTemplate redis;

    CreatedRoom room() {
        return rooms.create(new CreateRoomRequest(Theme.CASUAL, 5, "방장"));
    }

    ResultActions subscribe(String code, String token) throws Exception {
        return mvc.perform(get("/rooms/" + code + "/events").accept(MediaType.TEXT_EVENT_STREAM)
                .header("X-Participant-Token", token == null ? "" : token));
    }

    // 마지막으로 받은 이벤트 ID를 들고 재접속
    MockHttpServletResponse resume(CreatedRoom r, String lastEventId) throws Exception {
        return mvc.perform(get("/rooms/" + r.code() + "/events").accept(MediaType.TEXT_EVENT_STREAM)
                .header("X-Participant-Token", r.participantToken()).header("Last-Event-ID", lastEventId))
                .andExpect(request().asyncStarted()).andReturn().getResponse();
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

    @Test
    void joinIsBroadcast() throws Exception {
        CreatedRoom r = room();
        MockHttpServletResponse res = stream(r);
        participants.join(r.code(), new JoinRequest("준호"));
        assertThat(res.getContentAsString()).contains("id:2").contains("event:participant.joined")
                .contains("\"count\":2");
    }

    @Test
    void transitionIsBroadcast() throws Exception {
        CreatedRoom r = room();
        MockHttpServletResponse res = stream(r);
        participants.join(r.code(), new JoinRequest("준호"));
        redis.opsForHash().put(RoomKeys.room(r.code()), "state", RoomState.REVIEWING.name());
        machine.fire(r.code(), RoomTrigger.START_VOTING, Actor.HOST);
        assertThat(res.getContentAsString()).contains("id:3").contains("event:state.changed")
                .contains("\"from\":\"REVIEWING\"").contains("\"to\":\"VOTING\"");
    }

    @Test
    void otherRoomEventIsNotReceived() throws Exception {
        CreatedRoom mine = room();
        MockHttpServletResponse res = stream(mine);
        participants.join(room().code(), new JoinRequest("준호"));
        assertThat(res.getContentAsString()).doesNotContain("participant.joined");
    }

    @Test
    void reconnectReplaysMissedEvents() throws Exception {
        CreatedRoom r = room();
        participants.join(r.code(), new JoinRequest("준호"));
        participants.join(r.code(), new JoinRequest("서연"));
        String body = resume(r, "1").getContentAsString();
        assertThat(body).contains("id:2").contains("id:3").contains("event:ready");
        assertThat(body.indexOf("id:2")).isLessThan(body.indexOf("id:3"));
    }
}
