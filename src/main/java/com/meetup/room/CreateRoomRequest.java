package com.meetup.room;

import com.meetup.place.Theme;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/** 방 생성 요청. 방장도 별명으로 입장한 상태로 시작 */
public record CreateRoomRequest(
        @NotNull Theme theme,
        @NotNull @Min(2) Integer capacity,
        @NotBlank String nickname) {
}
