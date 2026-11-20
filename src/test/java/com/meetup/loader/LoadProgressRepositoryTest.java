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
}
