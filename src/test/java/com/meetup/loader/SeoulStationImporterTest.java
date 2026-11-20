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
}
