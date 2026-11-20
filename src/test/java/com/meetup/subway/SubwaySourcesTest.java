package com.meetup.subway;

import java.util.List;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class SubwaySourcesTest {

    final List<LineSegment> metro = SubwaySources.seoulMetro();

    boolean linked(List<LineSegment> segments, String line, String a, String b) {
        return segments.stream().anyMatch(s -> s.line().equals(line) && s.from().equals(a) && s.to().equals(b));
    }

    @Test
    void readsEveryMetroSegment() {
        // 279행 - 노선 시작 8행
        assertThat(metro).hasSize(271).allMatch(s -> s.minutes() != null && s.minutes() > 0);
    }

    @Test
    void branchesStartFromJunction() {
        assertThat(linked(metro, "2호선", "성수", "용답")).isTrue();
        assertThat(linked(metro, "2호선", "신도림", "도림천")).isTrue();
        assertThat(linked(metro, "5호선", "강동", "둔촌동")).isTrue();
        assertThat(linked(metro, "2호선", "시청", "용답")).isFalse();
    }

    @Test
    void loopsClose() {
        assertThat(linked(metro, "2호선", "충정로", "시청")).isTrue();
        assertThat(linked(metro, "6호선", "구산", "응암")).isTrue();
    }

    @Test
    void readsExtraLinesWithoutTimes() {
        var extra = SubwaySources.extraLines();
        // 130행 - 구간 시작 10행
        assertThat(extra).hasSize(120).allMatch(s -> s.minutes() == null);
        assertThat(linked(extra, "9호선", "여의도", "샛강")).isTrue();
        assertThat(linked(extra, "1호선", "구로", "가산디지털단지")).isTrue();
    }

    @Test
    void parsesMinutes() {
        assertThat(SubwaySources.minutes("01:30")).isEqualTo(1.5);
    }
}
