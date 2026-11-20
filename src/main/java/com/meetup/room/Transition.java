package com.meetup.room;

/** 전이 결과 */
public record Transition(RoomState from, RoomState state, long roomRev) {
}
