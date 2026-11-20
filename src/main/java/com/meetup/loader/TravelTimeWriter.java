package com.meetup.loader;

import java.util.List;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

/** 이동시간 벌크 저장 + 진행 완료 표시 (한 트랜잭션) */
@Repository
public class TravelTimeWriter {

    private final JdbcTemplate jdbc;

    public TravelTimeWriter(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    @Transactional
    public void save(List<LoadedRoute> routes) {
        jdbc.batchUpdate("INSERT INTO travel_time (station_id, candidate_id, minutes, transfers, source) "
                + "VALUES (?, ?, ?, ?, ?) ON CONFLICT (station_id, candidate_id) DO UPDATE SET "
                + "minutes = EXCLUDED.minutes, transfers = EXCLUDED.transfers, "
                + "source = EXCLUDED.source, updated_at = now()", routes, routes.size(), (ps, r) -> {
                    ps.setInt(1, r.key().stationId());
                    ps.setInt(2, r.key().candidateId());
                    ps.setInt(3, r.route().minutes());
                    ps.setInt(4, r.route().transfers());
                    ps.setString(5, r.route().source().name());
                });
        jdbc.batchUpdate("UPDATE load_progress SET status = 'DONE', attempts = attempts + 1, "
                + "last_error = NULL, updated_at = now() WHERE station_id = ? AND candidate_id = ?",
                routes, routes.size(), (ps, r) -> {
                    ps.setInt(1, r.key().stationId());
                    ps.setInt(2, r.key().candidateId());
                });
    }

    // 그래프 계산값 저장. ODsay 실측 행은 덮어쓰지 않음
    public void upsertGraph(List<LoadedRoute> routes) {
        jdbc.batchUpdate("INSERT INTO travel_time (station_id, candidate_id, minutes, transfers, source) "
                + "VALUES (?, ?, ?, ?, 'GRAPH') ON CONFLICT (station_id, candidate_id) DO UPDATE SET "
                + "minutes = EXCLUDED.minutes, transfers = EXCLUDED.transfers, source = 'GRAPH', "
                + "updated_at = now() WHERE travel_time.source <> 'ODSAY'", routes, 500, (ps, r) -> {
                    ps.setInt(1, r.key().stationId());
                    ps.setInt(2, r.key().candidateId());
                    ps.setInt(3, r.route().minutes());
                    ps.setInt(4, r.route().transfers());
                });
    }

    /** 조회 성공 조합 */
    public record LoadedRoute(LoadKey key, Route route) {
    }
}
