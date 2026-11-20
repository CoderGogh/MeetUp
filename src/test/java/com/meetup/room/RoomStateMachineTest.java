package com.meetup.room;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.meetup.common.ApiException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.event.ApplicationEvents;
import org.springframework.test.context.event.RecordApplicationEvents;

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
}
