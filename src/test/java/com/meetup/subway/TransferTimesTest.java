package com.meetup.subway;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;

class TransferTimesTest {

    final TransferTimes transfers = TransferTimes.load();

    @Test
    void readsOfficialTransferTime() {
        // 강남 2호선 → 신분당선 02:58
        assertThat(transfers.walk("강남", "2호선", "신분당선")).isCloseTo(2.97, within(0.01));
    }

    @Test
    void usesReverseDirectionWhenMissing() {
        assertThat(transfers.walk("강남", "신분당선", "2호선")).isCloseTo(2.97, within(0.01));
    }

    @Test
    void expressAndLocalShareLine() {
        assertThat(transfers.walk("여의도", "9호선", "9호선급행")).isZero();
    }

    @Test
    void unknownTransferIsNull() {
        assertThat(transfers.walk("없는역", "1호선", "2호선")).isNull();
    }
}
