package com.meetup.room;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import static com.meetup.room.RoomState.COLLECTING;
import static com.meetup.room.RoomState.CONFIRMED;
import static com.meetup.room.RoomState.EXPIRED;
import static com.meetup.room.RoomState.REVIEWING;
import static com.meetup.room.RoomState.VOTING;
import static com.meetup.room.RoomTrigger.CALCULATE_CANDIDATES;
import static com.meetup.room.RoomTrigger.CONFIRM;
import static com.meetup.room.RoomTrigger.PARTICIPANT_CHANGED;
import static com.meetup.room.RoomTrigger.RESET_INPUT;
import static com.meetup.room.RoomTrigger.START_VOTING;
import static org.assertj.core.api.Assertions.assertThat;

class RoomTransitionsTest {

    @Test
    void forwardPath() {
        assertThat(RoomTransitions.next(COLLECTING, CALCULATE_CANDIDATES)).contains(REVIEWING);
        assertThat(RoomTransitions.next(REVIEWING, START_VOTING)).contains(VOTING);
        assertThat(RoomTransitions.next(VOTING, CONFIRM)).contains(CONFIRMED);
    }

    @Test
    void allowedRollbacks() {
        assertThat(RoomTransitions.next(REVIEWING, PARTICIPANT_CHANGED)).contains(COLLECTING);
        assertThat(RoomTransitions.next(VOTING, RESET_INPUT)).contains(COLLECTING);
    }

    @Test
    void rejectsSkippingSteps() {
        assertThat(RoomTransitions.next(COLLECTING, START_VOTING)).isEmpty();
        assertThat(RoomTransitions.next(COLLECTING, CONFIRM)).isEmpty();
        assertThat(RoomTransitions.next(REVIEWING, CONFIRM)).isEmpty();
    }

    @Test
    void rejectsOtherRollbacks() {
        assertThat(RoomTransitions.next(REVIEWING, RESET_INPUT)).isEmpty();
        assertThat(RoomTransitions.next(VOTING, CALCULATE_CANDIDATES)).isEmpty();
    }

    @ParameterizedTest
    @EnumSource(RoomTrigger.class)
    void confirmedAndExpiredAreFinal(RoomTrigger trigger) {
        assertThat(RoomTransitions.next(EXPIRED, trigger)).isEmpty();
        if (trigger != RoomTrigger.EXPIRE) {
            assertThat(RoomTransitions.next(CONFIRMED, trigger)).isEmpty();
        }
    }

    @Test
    void onlyHostTriggersAreHostOnly() {
        assertThat(START_VOTING.hostOnly()).isTrue();
        assertThat(PARTICIPANT_CHANGED.hostOnly()).isFalse();
    }
}
