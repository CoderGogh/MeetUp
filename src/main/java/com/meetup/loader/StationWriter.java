package com.meetup.loader;

import java.util.Collection;
import java.util.List;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

/** 기준역 저장. 이미 있는 역은 노선·좌표 갱신 (후보 기준역 포함) */
@Repository
public class StationWriter {

    private final JdbcTemplate jdbc;

    public StationWriter(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public void upsert(Collection<StationRecord> stations) {
        List<StationRecord> rows = List.copyOf(stations);
        jdbc.batchUpdate("INSERT INTO station (name, lines, geom) "
                + "VALUES (?, ?, ST_SetSRID(ST_MakePoint(?, ?), 4326)) "
                + "ON CONFLICT (name) DO UPDATE SET lines = EXCLUDED.lines, geom = EXCLUDED.geom",
                rows, rows.size(), (ps, s) -> {
                    ps.setString(1, s.name());
                    ps.setString(2, String.join(",", s.lines()));
                    ps.setDouble(3, s.lng());
                    ps.setDouble(4, s.lat());
                });
    }
}
