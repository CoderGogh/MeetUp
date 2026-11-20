package com.meetup.room;

import com.meetup.place.Theme;
import java.time.Instant;

/** 방 (Redis 해시). hostTokenHash는 응답에 싣지 않음 */
public record Room(String code, Theme theme, int capacity, RoomState state, long rev,
        Instant createdAt, Instant expiresAt, String hostTokenHash) {

    // 만료 시각이 지나면 저장된 단계와 상관없이 EXPIRED
    public RoomState stateAt(Instant now) {
        return now.isBefore(expiresAt) ? state : RoomState.EXPIRED;
    }
}
