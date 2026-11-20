package com.meetup.subway;

import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;

class SubwayGraphTest {

    // 정차 0, 환승 대기 1, 환승 보행 기본 2, 첫 대기 0, 추정 60km/h·우회 1.0
    final GraphParams p = new GraphParams(0, 1, 2, 0, 60, 1.0);
    final TransferTimes none = TransferTimes.load();

    SubwayGraph graph(LineSegment... segments) {
        return SubwayGraph.build(List.of(segments), Map.of(), none, p);
    }

    @Test
    void sumsRideTimesOnOneLine() {
        var g = graph(new LineSegment("X", "가", "나", 2.0), new LineSegment("X", "나", "다", 3.0));
        var path = g.shortestFrom("가", 0).get("다");
        assertThat(path.minutes()).isEqualTo(5.0);
        assertThat(path.transfers()).isZero();
    }

    @Test
    void addsTransferWalkAndWait() {
        var g = graph(new LineSegment("X", "가", "나", 2.0), new LineSegment("Y", "나", "라", 1.0));
        var path = g.shortestFrom("가", 0).get("라");
        assertThat(path.minutes()).isEqualTo(2 + 2 + 1 + 1.0);
        assertThat(path.transfers()).isEqualTo(1);
    }

    @Test
    void prefersFasterRouteWithTransfer() {
        var g = graph(new LineSegment("X", "가", "나", 1.0), new LineSegment("X", "나", "다", 20.0),
                new LineSegment("Y", "나", "다", 2.0));
        assertThat(g.shortestFrom("가", 0).get("다").minutes()).isEqualTo(1 + 3 + 2.0);
    }
}
