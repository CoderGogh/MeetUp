package com.meetup.room;

import jakarta.validation.constraints.NotNull;

/** 전이 요청 */
public record TransitionRequest(@NotNull RoomTrigger trigger) {
}
