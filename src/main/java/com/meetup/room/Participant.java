package com.meetup.room;

/** 참여자. 토큰 원문은 담지 않음 */
public record Participant(String id, String nickname, boolean host, boolean originSubmitted, long joinedAt) {
}
