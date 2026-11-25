package com.meetup.realtime;

import com.meetup.room.Actor;
import com.meetup.room.JoinRequest;
import com.meetup.room.ParticipantService;
import com.meetup.room.RoomKeys;
import com.meetup.room.RoomService;
import com.meetup.room.RoomState;
import com.meetup.room.RoomStateMachine;
import com.meetup.room.RoomTrigger;
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

    // 끊긴 동안 입장 1건(rev 3) + 투표 시작(rev 4)
    void changeWhileAway(String code) {
        participants.join(code, new JoinRequest("서연"));
        redis.opsForHash().put(RoomKeys.room(code), "state", RoomState.REVIEWING.name());
        machine.fire(code, RoomTrigger.START_VOTING, Actor.HOST);
    }
}
