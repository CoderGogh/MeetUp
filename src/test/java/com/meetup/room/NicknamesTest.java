package com.meetup.room;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class NicknamesTest {

    @Test
    void stripsSpaces() {
        assertThat(Nicknames.clean("  민지 ")).isEqualTo("민지");
    }
}
