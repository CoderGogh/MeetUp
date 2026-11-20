package com.meetup.loader;

import com.meetup.place.PlaceRepository;
import com.meetup.subway.GraphParams;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static org.assertj.core.api.Assertions.assertThat;

class GraphMatrixLoaderTest extends LoaderDbTest {

    @Autowired
    PlaceRepository places;
    @Autowired
    TravelTimeWriter writer;
    @Autowired
    OdsaySampleRepository samples;

    GraphReport run() {
        return new GraphMatrixLoader(places, writer, samples, GraphParams.DEFAULT).run();
    }

    int minutes(String from, String to) {
        return count("SELECT t.minutes FROM travel_time t JOIN station s ON s.id = t.station_id "
                + "JOIN candidate c ON c.id = t.candidate_id WHERE s.name = ? AND c.name = ?", from, to);
    }

    @Test
    void storesReachablePairsAsGraph() {
        // 테스트 DB는 후보 기준역 30개뿐이라 좌표 없는 추정 구간은 빠짐 (실데이터는 304역 전부 연결)
        GraphReport r = run();
        assertThat(r.skippedSegments()).isPositive();
        assertThat(r.pairs()).isGreaterThan(pairs() * 9 / 10);
        assertThat(count("SELECT count(*) FROM travel_time WHERE source = 'GRAPH'")).isEqualTo(r.pairs());
    }

    @Test
    void sameStationIsZero() {
        run();
        assertThat(minutes("강남", "강남")).isZero();
    }

    @Test
    void keepsOdsayMeasurement() {
        jdbc.update("INSERT INTO travel_time (station_id, candidate_id, minutes, source) "
                + "SELECT s.id, c.id, 13, 'ODSAY' FROM station s, candidate c WHERE s.name = '강남' AND c.name = '잠실'");
        GraphReport r = run();
        assertThat(minutes("강남", "잠실")).isEqualTo(13);
        assertThat(r.samples()).isEqualTo(1);
    }
}
