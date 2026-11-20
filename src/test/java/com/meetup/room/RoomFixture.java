package com.meetup.room;

import com.meetup.place.Theme;
import org.springframework.data.redis.core.StringRedisTemplate;

/** 방 테스트 준비 (서비스로 만들고 Redis를 직접 조작) */
final class RoomFixture {

    private RoomFixture() {
    }

    static CreatedRoom room(RoomService rooms, int capacity) {
        return rooms.create(new CreateRoomRequest(Theme.CASUAL, capacity, "방장"));
    }

    static void setState(StringRedisTemplate redis, String code, RoomState state) {
        redis.opsForHash().put(RoomKeys.room(code), "state", state.name());
    }

    static void expire(StringRedisTemplate redis, String code) {
        redis.opsForHash().put(RoomKeys.room(code), "expiresAt", String.valueOf(System.currentTimeMillis() - 1000));
    }
}
