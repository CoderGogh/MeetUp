package com.meetup.room;

import java.util.EnumMap;
import java.util.Map;
import java.util.Optional;

/** 전이 허용 목록. 목록에 없는 (단계, 이벤트) 쌍은 전부 금지 (RFP 8.4.1·8.4.2) */
public final class RoomTransitions {

    private static final Map<RoomState, Map<RoomTrigger, RoomState>> ALLOWED = new EnumMap<>(RoomState.class);

    static {
        allow(RoomState.COLLECTING, RoomTrigger.CALCULATE_CANDIDATES, RoomState.REVIEWING);
        allow(RoomState.REVIEWING, RoomTrigger.START_VOTING, RoomState.VOTING);
        allow(RoomState.REVIEWING, RoomTrigger.PARTICIPANT_CHANGED, RoomState.COLLECTING);
        allow(RoomState.VOTING, RoomTrigger.RESET_INPUT, RoomState.COLLECTING);
        allow(RoomState.VOTING, RoomTrigger.CONFIRM, RoomState.CONFIRMED);
    }

    private RoomTransitions() {
    }

    private static void allow(RoomState from, RoomTrigger trigger, RoomState to) {
        ALLOWED.computeIfAbsent(from, s -> new EnumMap<>(RoomTrigger.class)).put(trigger, to);
    }

    public static Optional<RoomState> next(RoomState from, RoomTrigger trigger) {
        return Optional.ofNullable(ALLOWED.getOrDefault(from, Map.of()).get(trigger));
    }
}
