package com.meetup.loader;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

/** 이동시간 벌크 저장 + 진행 완료 표시 (한 트랜잭션) */
@Repository
public class TravelTimeWriter {

    private final JdbcTemplate jdbc;

    public TravelTimeWriter(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    /** 조회 성공 조합 */
    public record LoadedRoute(LoadKey key, Route route) {
    }
}
