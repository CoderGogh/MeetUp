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
}
