package com.meetup.room;

import com.meetup.common.ApiException;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class NicknamesTest {

    @Test
    void stripsSpaces() {
        assertThat(Nicknames.clean("  민지 ")).isEqualTo("민지");
    }

    @Test
    void allowsTwelveCharacters() {
        assertThat(Nicknames.clean("가나다라마바사아자차카타")).hasSize(12);
        assertThatThrownBy(() -> Nicknames.clean("가나다라마바사아자차카타파")).isInstanceOf(ApiException.class);
    }

    @Test
    void rejectsBlankAndInvisible() {
        assertThatThrownBy(() -> Nicknames.clean("   ")).isInstanceOf(ApiException.class);
        assertThatThrownBy(() -> Nicknames.clean("민​지")).isInstanceOf(ApiException.class);
        assertThatThrownBy(() -> Nicknames.clean("민\n지")).isInstanceOf(ApiException.class);
    }
}
