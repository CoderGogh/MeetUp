package com.meetup.realtime;

import com.meetup.room.RoomEvent;
import java.util.List;

/** 끊긴 동안의 이벤트. resync면 보관 범위 밖이라 전체 재조회 안내 */
public record Replay(List<RoomEvent> events, boolean resync, long roomRev) {

    static Replay none(long roomRev) {
        return new Replay(List.of(), false, roomRev);
    }

    static Replay resync(long roomRev) {
        return new Replay(List.of(), true, roomRev);
    }
}
