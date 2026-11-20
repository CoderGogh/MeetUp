package com.meetup.room;

import java.time.Instant;

/** 방 생성 응답. 토큰 원문은 이 응답에서만 내려감 */
public record CreatedRoom(String code, String inviteUrl, RoomState state, long roomRev, Instant expiresAt,
        String participantId, String participantToken, String hostToken) {
}
