package com.meetup.loader;

import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;

/** 적재 DB 테스트 공통 (시드 외 데이터 정리) */
@SpringBootTest
@ActiveProfiles("test")
abstract class LoaderDbTest {

    @Autowired
    JdbcTemplate jdbc;

    @BeforeEach
    void cleanLoadData() {
        jdbc.update("DELETE FROM travel_time");
        jdbc.update("DELETE FROM load_progress");
        jdbc.update("DELETE FROM station WHERE id NOT IN (SELECT station_id FROM candidate)");
    }

    int count(String sql, Object... args) {
        return jdbc.queryForObject(sql, Integer.class, args);
    }

    // 역 × 후보지 전체 조합 수
    int pairs() {
        return count("SELECT (SELECT count(*) FROM station) * (SELECT count(*) FROM candidate)");
    }
}
