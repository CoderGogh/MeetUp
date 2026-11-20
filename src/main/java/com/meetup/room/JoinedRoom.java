package com.meetup.room;

/** 입장 응답. 토큰 원문은 여기서만 */
public record JoinedRoom(String participantId, String participantToken, long roomRev) {
}
