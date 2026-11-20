package com.meetup.loader;

import java.util.List;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

/** 적재 진행 기록 (행 단위) */
@Repository
public class LoadProgressRepository {

    private static final String UNFINISHED =
            "status = 'PENDING' OR (status = 'FAILED' AND attempts < ?)";

    private final JdbcTemplate jdbc;

    public LoadProgressRepository(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    // 새 역·후보지 조합만 추가
    public int plan() {
        return jdbc.update("INSERT INTO load_progress (station_id, candidate_id) "
                + "SELECT s.id, c.id FROM station s CROSS JOIN candidate c ON CONFLICT DO NOTHING");
    }

    // 미완료 우선, 실패 조합은 뒤로
    public List<LoadKey> next(int limit, int maxAttempts) {
        return jdbc.query("SELECT station_id, candidate_id FROM load_progress WHERE " + UNFINISHED
                        + " ORDER BY status DESC, station_id, candidate_id LIMIT ?",
                (rs, i) -> new LoadKey(rs.getInt(1), rs.getInt(2)), maxAttempts, limit);
    }

    public void markFailed(LoadKey key, String error) {
        jdbc.update("UPDATE load_progress SET status = 'FAILED', attempts = attempts + 1, "
                        + "last_error = left(?, 200), updated_at = now() WHERE station_id = ? AND candidate_id = ?",
                error, key.stationId(), key.candidateId());
    }

    public int remaining(int maxAttempts) {
        return jdbc.queryForObject("SELECT count(*) FROM load_progress WHERE " + UNFINISHED,
                Integer.class, maxAttempts);
    }
}
