package com.meetup.room;

import com.meetup.common.ApiException;
import com.meetup.common.ErrorCode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.event.ApplicationEvents;
import org.springframework.test.context.event.RecordApplicationEvents;

@SpringBootTest
@ActiveProfiles("test")
@RecordApplicationEvents
class ParticipantJoinTest {

    @Autowired
    RoomService rooms;
    @Autowired
    ParticipantService participants;
    @Autowired
    StringRedisTemplate redis;
    @Autowired
    ApplicationEvents events;

    JoinedRoom join(String code, String nickname) {
        return participants.join(code, new JoinRequest(nickname));
    }

    ErrorCode failure(Runnable action) {
        try {
            action.run();
            return null;
        } catch (ApiException e) {
            return e.code();
        }
    }
}
