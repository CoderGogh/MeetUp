package com.meetup.loader;

import java.time.Clock;
import java.time.YearMonth;
import java.time.ZoneId;
import java.util.List;
import org.springframework.jdbc.core.JdbcTemplate;

/** 월별 호출 장부 (DB). 실행·설정과 무관하게 한 달 한도를 넘지 않음 */
public class MonthlyCallBudget implements CallBudget {

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

    // 한도 안일 때만 1 증가 (조건부 upsert라 동시 실행에도 초과 없음)
    @Override
    public void acquire() {
        if (limit > 0) {
            List<Integer> after = jdbc.queryForList("INSERT INTO api_call_budget (provider, month, calls) "
                    + "VALUES (?, ?, 1) ON CONFLICT (provider, month) DO UPDATE "
                    + "SET calls = api_call_budget.calls + 1 WHERE api_call_budget.calls < ? RETURNING calls",
                    Integer.class, provider, month(), limit);
            if (!after.isEmpty()) {
                return;
            }
        }
        throw new QuotaExceededException(provider + " 월 호출 한도 " + limit + "건 소진");
    }
}
