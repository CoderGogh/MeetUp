package com.meetup.room;

import org.junit.jupiter.api.Test;

import static com.meetup.room.RoomState.COLLECTING;
import static com.meetup.room.RoomState.CONFIRMED;
import static com.meetup.room.RoomState.REVIEWING;
import static com.meetup.room.RoomState.VOTING;
import static com.meetup.room.RoomTrigger.CALCULATE_CANDIDATES;
import static com.meetup.room.RoomTrigger.CONFIRM;
import static com.meetup.room.RoomTrigger.START_VOTING;
import static org.assertj.core.api.Assertions.assertThat;

class RoomTransitionsTest {

    @Test
    void forwardPath() {
        assertThat(RoomTransitions.next(COLLECTING, CALCULATE_CANDIDATES)).contains(REVIEWING);
        assertThat(RoomTransitions.next(REVIEWING, START_VOTING)).contains(VOTING);
        assertThat(RoomTransitions.next(VOTING, CONFIRM)).contains(CONFIRMED);
    }
}
