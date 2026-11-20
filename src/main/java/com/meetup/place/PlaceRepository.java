package com.meetup.place;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.EnumSet;
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

    public List<Candidate> findCandidates() {
        return jdbc.query("SELECT id, name, station_id, area_type, region, theme_tags, underground, "
                + "ST_Y(geom) AS lat, ST_X(geom) AS lng FROM candidate ORDER BY id", (rs, i) -> candidate(rs));
    }

    private static Candidate candidate(ResultSet rs) throws SQLException {
        var themes = EnumSet.noneOf(Theme.class);
        Arrays.stream((String[]) rs.getArray("theme_tags").getArray()).map(Theme::valueOf).forEach(themes::add);
        return new Candidate(rs.getInt("id"), rs.getString("name"), rs.getInt("station_id"),
                AreaType.valueOf(rs.getString("area_type")), Region.valueOf(rs.getString("region")),
                themes, rs.getBoolean("underground"), rs.getDouble("lat"), rs.getDouble("lng"));
    }
}
