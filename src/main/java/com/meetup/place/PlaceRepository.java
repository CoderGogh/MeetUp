package com.meetup.place;

import java.util.List;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

/** 기준역·후보지 조회 */
@Repository
public class PlaceRepository {

    private final JdbcTemplate jdbc;

    public PlaceRepository(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public List<Station> findStations() {
        return jdbc.query("SELECT id, name, lines, ST_Y(geom) AS lat, ST_X(geom) AS lng "
                + "FROM station ORDER BY id", (rs, i) -> new Station(rs.getInt("id"), rs.getString("name"),
                List.of(rs.getString("lines").split(",")), rs.getDouble("lat"), rs.getDouble("lng")));
    }
}
