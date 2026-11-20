package com.meetup.subway;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class StationNamesTest {

    @Test
    void stripsParentheses() {
        assertThat(StationNames.normalize("청량리(서울시립대입구)")).isEqualTo("청량리");
        assertThat(StationNames.normalize("미아사거리 ")).isEqualTo("미아사거리");
    }

    @Test
    void mapsRenamedStations() {
        assertThat(StationNames.normalize("당고개")).isEqualTo("불암산");
        assertThat(StationNames.normalize("이수")).isEqualTo("총신대입구");
        assertThat(StationNames.normalize("총신대입구(이수)")).isEqualTo("총신대입구");
    }
}
