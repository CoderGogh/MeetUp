package com.meetup.room;

import jakarta.validation.constraints.NotBlank;

/** 입장 요청 */
public record JoinRequest(@NotBlank String nickname) {
}
