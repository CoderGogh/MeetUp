package com.meetup.room;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
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

    // 전원 출발지 입력 완료로 표시 (출발지 입력은 4주차)
    static void submitOrigins(StringRedisTemplate redis, ObjectMapper json, String code) throws Exception {
        for (var e : redis.opsForHash().entries(RoomKeys.participants(code)).entrySet()) {
            ObjectNode p = (ObjectNode) json.readTree(e.getValue().toString());
            p.put("originSubmitted", true);
            redis.opsForHash().put(RoomKeys.participants(code), e.getKey(), p.toString());
        }
    }

    static void decideLeader(StringRedisTemplate redis, String code) {
        redis.opsForHash().put(RoomKeys.room(code), "leaderDecided", "1");
    }
}
