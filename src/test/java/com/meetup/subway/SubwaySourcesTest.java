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
}
