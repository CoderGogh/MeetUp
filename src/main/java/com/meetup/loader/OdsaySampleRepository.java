package com.meetup.loader;

import java.util.List;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

/** 행렬에 저장된 ODsay 실측값 (그래프 오차 측정용) */
@Repository
public class OdsaySampleRepository {

    private final JdbcTemplate jdbc;

    public OdsaySampleRepository(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public List<Sample> findAll() {
        return jdbc.query("SELECT station_id, candidate_id, minutes FROM travel_time WHERE source = 'ODSAY'",
                (rs, i) -> new Sample(new LoadKey(rs.getInt(1), rs.getInt(2)), rs.getInt(3)));
    }

    /** 실측 한 건 */
    public record Sample(LoadKey key, int minutes) {
    }
}
