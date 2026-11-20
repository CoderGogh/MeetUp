package com.meetup.loader;

import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static org.assertj.core.api.Assertions.assertThat;

class SeoulStationImporterTest extends LoaderDbTest {

    @Autowired
    StationWriter writer;

    SeoulStationImporter importer(MasterStation... rows) {
        SeoulStationClient fake = new SeoulStationClient(null, "dummy") {
            @Override
            public List<MasterStation> fetch() {
                return List.of(rows);
            }
        };
        return new SeoulStationImporter(fake, writer);
    }

    String lines(String name) {
        return jdbc.queryForObject("SELECT lines FROM station WHERE name = ?", String.class, name);
    }

    @Test
    void importsGraphStationWithGraphLines() {
        importer(new MasterStation("양재시민의숲(매헌)", "신분당선", 37.4704, 127.0384)).importStations();
        assertThat(lines("양재시민의숲")).isEqualTo("신분당선");
    }

    @Test
    void skipsLinesOutsideGraph() {
        int n = importer(new MasterStation("솔샘", "우이신설선", 37.62, 127.01),
                new MasterStation("신촌", "경의중앙선", 37.5598, 126.9426)).importStations();
        assertThat(n).isZero();
    }

    @Test
    void updatesSeededCandidateStation() {
        importer(new MasterStation("강남", "2호선", 37.497952, 127.027619)).importStations();
        Double lat = jdbc.queryForObject("SELECT ST_Y(geom) FROM station WHERE name = '강남'", Double.class);
        assertThat(lat).isEqualTo(37.497952);
        assertThat(lines("강남")).isEqualTo("2호선,신분당선");
    }

    @Test
    void graphCoversEveryCandidate() {
        var graph = SeoulStationImporter.graphLines().keySet();
        assertThat(jdbc.queryForList("SELECT name FROM candidate", String.class)).allMatch(graph::contains);
    }

    @Test
    void fallsBackToNameWhenLineDiffers() {
        importer(new MasterStation("한남", "경원선", 37.5294, 127.0090)).importStations();
        assertThat(lines("한남")).isEqualTo("경의중앙선");
    }
}
