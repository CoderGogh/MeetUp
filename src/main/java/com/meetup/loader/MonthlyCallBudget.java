package com.meetup.loader;

import java.time.Clock;
import java.time.YearMonth;
import java.time.ZoneId;
import org.springframework.jdbc.core.JdbcTemplate;

/** 월별 호출 장부 (DB). 실행·설정과 무관하게 한 달 한도를 넘지 않음 */
public class MonthlyCallBudget {

    private static final ZoneId KST = ZoneId.of("Asia/Seoul");

    private final JdbcTemplate jdbc;
    private final String provider;
    private final int limit;
    private final Clock clock;

    public MonthlyCallBudget(JdbcTemplate jdbc, String provider, int limit, Clock clock) {
        this.jdbc = jdbc;
        this.provider = provider;
        this.limit = limit;
        this.clock = clock;
    }

    private String month() {
        return YearMonth.now(clock.withZone(KST)).toString();
    }
}
