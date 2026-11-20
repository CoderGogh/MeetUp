package com.meetup.loader;

import java.time.Duration;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static org.assertj.core.api.Assertions.assertThat;

class LoadProgressRepositoryTest extends LoaderDbTest {

    @Autowired
    LoadProgressRepository progress;

    // 지정 조합 외 전부 완료 처리
    void finishAllExcept(LoadKey key) {
        jdbc.update("UPDATE load_progress SET status = 'DONE' WHERE NOT (station_id = ? AND candidate_id = ?)",
                key.stationId(), key.candidateId());
    }

    @Test
    void planCoversEveryPair() {
        assertThat(progress.plan()).isEqualTo(pairs());
    }

    @Test
    void planAddsOnlyNewPairs() {
        progress.plan();
        assertThat(progress.plan()).isZero();
    }

    @Test
    void failedComesAfterPending() {
        progress.plan();
        LoadKey first = progress.next(1, 3).get(0);
        progress.markFailed(first, "ODsay HTTP 500");
        assertThat(progress.next(1, 3)).doesNotContain(first);
        finishAllExcept(first);
        assertThat(progress.next(1, 3)).containsExactly(first);
    }

    @Test
    void stopsRetryAtMaxAttempts() {
        progress.plan();
        LoadKey key = progress.next(1, 3).get(0);
        finishAllExcept(key);
        for (int i = 0; i < 3; i++) {
            progress.markFailed(key, "ODsay HTTP 500");
        }
        assertThat(progress.next(10, 3)).isEmpty();
        assertThat(progress.remaining(3)).isZero();
    }

    @Test
    void requeuesStaleRows() {
        progress.plan();
        LoadKey key = progress.next(1, 3).get(0);
        jdbc.update("UPDATE load_progress SET status = 'DONE'");
        jdbc.update("INSERT INTO travel_time (station_id, candidate_id, minutes, source, updated_at) "
                + "VALUES (?, ?, 30, 'ODSAY', now() - interval '400 days')", key.stationId(), key.candidateId());
        assertThat(progress.requeueStale(Duration.ofDays(180))).isEqualTo(1);
        assertThat(progress.next(10, 3)).containsExactly(key);
    }
}
