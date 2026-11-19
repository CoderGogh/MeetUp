package com.meetup.place;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
class MigrationTest {

    @Autowired
    JdbcTemplate jdbc;

    @Test
    void referenceTablesExist() {
        var tables = jdbc.queryForList(
                "SELECT table_name FROM information_schema.tables WHERE table_schema = 'public'", String.class);
        assertThat(tables).contains("station", "candidate", "travel_time", "load_progress");
    }

    @Test
    void stationHasSpatialIndex() {
        String def = jdbc.queryForObject(
                "SELECT indexdef FROM pg_indexes WHERE indexname = 'idx_station_geom'", String.class);
        assertThat(def).containsIgnoringCase("gist");
    }

    @Test
    void travelTimeRejectsNegativeMinutes() {
        String sql = "SELECT pg_get_constraintdef(oid) FROM pg_constraint "
                + "WHERE conrelid = 'travel_time'::regclass AND contype = 'c'";
        assertThat(jdbc.queryForList(sql, String.class)).anyMatch(d -> d.contains("minutes >= 0"));
    }
}
