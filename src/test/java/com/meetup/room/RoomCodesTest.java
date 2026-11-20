package com.meetup.room;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.IntStream;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class RoomCodesTest {

    @Test
    void usesUnambiguousAlphabet() {
        String code = RoomCodes.generate();
        assertThat(code).hasSize(12).matches("[23456789abcdefghjkmnpqrstuvwxyz]+");
    }

    @Test
    void doesNotRepeatInManySamples() {
        Set<String> codes = new HashSet<>();
        IntStream.range(0, 10_000).forEach(i -> codes.add(RoomCodes.generate()));
        assertThat(codes).hasSize(10_000);
    }

    @Test
    void rejectsMalformedCodes() {
        assertThat(RoomCodes.isValid(RoomCodes.generate())).isTrue();
        assertThat(RoomCodes.isValid("abc")).isFalse();
        assertThat(RoomCodes.isValid("ABCDEFGHJKMN")).isFalse();
        assertThat(RoomCodes.isValid("room:{x}:tok")).isFalse();
        assertThat(RoomCodes.isValid(null)).isFalse();
    }
}
