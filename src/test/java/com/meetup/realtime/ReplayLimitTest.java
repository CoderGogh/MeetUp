package com.meetup.realtime;

import com.meetup.place.Theme;
import com.meetup.room.CreateRoomRequest;
import com.meetup.room.CreatedRoom;
import com.meetup.room.JoinRequest;
import com.meetup.room.ParticipantService;
import com.meetup.room.RoomEvent;
import com.meetup.room.RoomKeys;
import com.meetup.room.RoomService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;

/** 방별 최근 3개만 보관할 때 재전송·재조회 안내 */
@SpringBootTest(properties = "meetup.realtime.retained-events=3")
@ActiveProfiles("test")
class ReplayLimitTest {

    @Autowired
    RoomService rooms;
    @Autowired
    ParticipantService participants;
    @Autowired
    RoomEventLog events;
    @Autowired
    StringRedisTemplate redis;

    // 방장 + 4명 입장 → rev 2~5 이벤트
    CreatedRoom roomWithFourJoins() {
        CreatedRoom r = rooms.create(new CreateRoomRequest(Theme.CASUAL, 10, "방장"));
        for (int i = 1; i <= 4; i++) {
            participants.join(r.code(), new JoinRequest("팀원" + i));
        }
        return r;
    }

    @Test
    void keepsOnlyRecentEvents() {
        CreatedRoom r = roomWithFourJoins();
        assertThat(events.after(r.code(), 0)).extracting(RoomEvent::roomRev).containsExactly(3L, 4L, 5L);
    }

    @Test
    void replayWithinRetention() {
        CreatedRoom r = roomWithFourJoins();
        Replay replay = events.since(r.code(), 2, 5);
        assertThat(replay.resync()).isFalse();
        assertThat(replay.events()).extracting(RoomEvent::roomRev).containsExactly(3L, 4L, 5L);
    }

    @Test
    void beyondRetentionIsResync() {
        CreatedRoom r = roomWithFourJoins();
        assertThat(events.since(r.code(), 1, 5).resync()).isTrue();
    }

    @Test
    void upToDateIsNeitherReplayNorResync() {
        CreatedRoom r = roomWithFourJoins();
        Replay replay = events.since(r.code(), 5, 5);
        assertThat(replay.resync()).isFalse();
        assertThat(replay.events()).isEmpty();
    }

    @Test
    void eventsExpireWithRoom() {
        CreatedRoom r = roomWithFourJoins();
        Long room = redis.getExpire(RoomKeys.room(r.code()));
        assertThat(redis.getExpire(RoomKeys.events(r.code()))).isPositive().isLessThanOrEqualTo(room);
    }
}
