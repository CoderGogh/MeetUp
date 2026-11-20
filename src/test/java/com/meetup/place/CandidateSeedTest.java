package com.meetup.place;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;

/** 후보지 큐레이션 규칙 (docs/candidates.md) */
@SpringBootTest
@ActiveProfiles("test")
class CandidateSeedTest {

    @Autowired
    JdbcTemplate jdbc;

    @Test
    void seedsThirtyCandidates() {
        Integer count = jdbc.queryForObject("SELECT count(*) FROM candidate", Integer.class);
        assertThat(count).isEqualTo(30);
    }

    @Test
    void everyRegionHasAtLeastFive() {
        var counts = jdbc.queryForList(
                "SELECT count(*) FROM candidate GROUP BY region", Integer.class);
        assertThat(counts).hasSize(Region.values().length).allMatch(c -> c >= 5);
    }

    @Test
    void noLineCoversMoreThanHalf() {
        String sql = "SELECT max(n) FROM (SELECT count(*) AS n FROM candidate c "
                + "JOIN station s ON s.id = c.station_id "
                + "CROSS JOIN unnest(string_to_array(s.lines, ',')) AS line GROUP BY line) t";
        assertThat(jdbc.queryForObject(sql, Integer.class)).isLessThanOrEqualTo(15);
    }

    @Test
    void everyCandidateHasTheme() {
        String sql = "SELECT count(*) FROM candidate WHERE cardinality(theme_tags) = 0";
        assertThat(jdbc.queryForObject(sql, Integer.class)).isZero();
    }

    @Test
    void themeTagsAreKnown() {
        var tags = jdbc.queryForList(
                "SELECT DISTINCT unnest(theme_tags) FROM candidate", String.class);
        assertThat(tags).allSatisfy(t -> Theme.valueOf(t));
    }

    // 조건에 걸리는 후보명
    private java.util.List<String> violating(String where) {
        return jdbc.queryForList("SELECT c.name FROM candidate c "
                + "JOIN station s ON s.id = c.station_id WHERE " + where, String.class);
    }

    @Test
    void indoorRequiresUnderground() {
        assertThat(violating("'INDOOR' = ANY (c.theme_tags) AND NOT c.underground")).isEmpty();
    }
}
