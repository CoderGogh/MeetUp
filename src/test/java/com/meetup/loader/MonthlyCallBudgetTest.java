package com.meetup.loader;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class MonthlyCallBudgetTest extends LoaderDbTest {

    // 2026-11-30 23:30 KST
    final Clock nov = Clock.fixed(Instant.parse("2026-11-30T14:30:00Z"), ZoneOffset.UTC);

    MonthlyCallBudget budget(int limit, Clock clock) {
        return new MonthlyCallBudget(jdbc, "TEST", limit, clock);
    }

    @BeforeEach
    void cleanBudget() {
        jdbc.update("DELETE FROM api_call_budget WHERE provider = 'TEST'");
    }

    @Test
    void stopsAtMonthlyLimit() {
        var b = budget(2, nov);
        b.acquire();
        b.acquire();
        assertThatThrownBy(b::acquire).isInstanceOf(QuotaExceededException.class);
        assertThat(b.used()).isEqualTo(2);
        assertThat(b.remaining()).isZero();
    }

    @Test
    void sharesLedgerAcrossInstances() {
        budget(2, nov).acquire();
        budget(2, nov).acquire();
        assertThatThrownBy(budget(2, nov)::acquire).isInstanceOf(QuotaExceededException.class);
    }
}
