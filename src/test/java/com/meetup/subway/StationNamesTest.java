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

    @Test
    void mapsLineNames() {
        assertThat(StationNames.line("4")).isEqualTo("4호선");
        assertThat(StationNames.line("경부선")).isEqualTo("1호선");
        assertThat(StationNames.line("9호선(연장)")).isEqualTo("9호선");
        assertThat(StationNames.line("신분당선(연장2)")).isEqualTo("신분당선");
        assertThat(StationNames.line("분당선")).isEqualTo("수인분당선");
    }
}
