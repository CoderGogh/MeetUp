package com.meetup.room;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.meetup.common.ApiException;
import com.meetup.common.ErrorCode;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.event.ApplicationEvents;
import org.springframework.test.context.event.RecordApplicationEvents;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
@RecordApplicationEvents
class RoomStateMachineTest {

    @Autowired
    RoomStateMachine machine;
    @Autowired
    RoomService rooms;
    @Autowired
    ParticipantService participants;
    @Autowired
    StringRedisTemplate redis;
    @Autowired
    ObjectMapper json;
    @Autowired
    ApplicationEvents events;

    String room() {
        return RoomFixture.room(rooms, 5).code();
    }

    ApiException failure(String code, RoomTrigger trigger, Actor actor) {
        try {
            machine.fire(code, trigger, actor);
            throw new AssertionError("전이가 거부되지 않음");
        } catch (ApiException e) {
            return e;
        }
    }

    @Test
    void hostMovesForwardAndBumpsRev() {
        String code = room();
        RoomFixture.setState(redis, code, RoomState.REVIEWING);
        Transition t = machine.fire(code, RoomTrigger.START_VOTING, Actor.HOST);
        assertThat(t.state()).isEqualTo(RoomState.VOTING);
        assertThat(t.roomRev()).isEqualTo(2);
    }

    @Test
    void memberCannotTransition() {
        assertThat(failure(room(), RoomTrigger.CALCULATE_CANDIDATES, Actor.MEMBER).code())
                .isEqualTo(ErrorCode.FORBIDDEN);
    }

    @Test
    void hostCannotFireSystemTrigger() {
        assertThat(failure(room(), RoomTrigger.PARTICIPANT_CHANGED, Actor.HOST).code())
                .isEqualTo(ErrorCode.FORBIDDEN);
    }

    @Test
    void forbiddenTransitionReturnsCurrentState() {
        ApiException e = failure(room(), RoomTrigger.START_VOTING, Actor.HOST);
        assertThat(e.code()).isEqualTo(ErrorCode.INVALID_TRANSITION);
        assertThat(e.details()).containsEntry("currentState", "COLLECTING").containsEntry("roomRev", 1L);
    }
}
