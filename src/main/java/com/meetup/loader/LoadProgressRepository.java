package com.meetup.loader;

import java.util.List;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

/** 적재 진행 기록 (행 단위) */
@Repository
public class LoadProgressRepository {

    private final JdbcTemplate jdbc;

    public LoadProgressRepository(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    // 새 역·후보지 조합만 추가
    public int plan() {
        return jdbc.update("INSERT INTO load_progress (station_id, candidate_id) "
                + "SELECT s.id, c.id FROM station s CROSS JOIN candidate c ON CONFLICT DO NOTHING");
    }
}
