package com.meetup.loader;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.concurrent.Callable;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.IntStream;
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

    @Test
    void resetsOnNextMonthInKst() {
        budget(1, nov).acquire();
        // 30분 뒤 = 12월 1일 00:00 KST
        var dec = Clock.offset(nov, java.time.Duration.ofMinutes(30));
        assertThat(budget(1, dec).remaining()).isEqualTo(1);
    }

    @Test
    void zeroLimitNeverCalls() {
        assertThatThrownBy(budget(0, nov)::acquire).isInstanceOf(QuotaExceededException.class);
        assertThat(budget(0, nov).used()).isZero();
    }

    @Test
    void concurrentAcquireNeverExceedsLimit() throws Exception {
        var b = budget(5, nov);
        var ok = new AtomicInteger();
        var pool = Executors.newFixedThreadPool(10);
        var tasks = IntStream.range(0, 20).<Callable<Void>>mapToObj(i -> () -> {
            try {
                b.acquire();
                ok.incrementAndGet();
            } catch (QuotaExceededException e) {
                // 한도 초과
            }
            return null;
        }).toList();
        pool.invokeAll(tasks);
        pool.shutdown();
        assertThat(ok.get()).isEqualTo(5);
        assertThat(b.used()).isEqualTo(5);
    }
}
