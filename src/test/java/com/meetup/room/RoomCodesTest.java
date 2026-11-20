package com.meetup.room;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class RoomCodesTest {

    @Test
    void usesUnambiguousAlphabet() {
        String code = RoomCodes.generate();
        assertThat(code).hasSize(12).matches("[23456789abcdefghjkmnpqrstuvwxyz]+");
    }
}
