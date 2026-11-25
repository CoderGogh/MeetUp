package com.meetup.realtime;

import com.meetup.room.ParticipantService;
import com.meetup.room.RoomService;
import com.meetup.room.RoomStateMachine;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.test.context.ActiveProfiles;


/** NFR-07: 강제 단절 후 재접속하면 끊긴 동안의 변경을 받아 상태가 일치 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
class ReconnectTest {

    @Autowired
    RoomService rooms;
    @Autowired
    ParticipantService participants;
    @Autowired
    RoomStateMachine machine;
    @Autowired
    StringRedisTemplate redis;
}
