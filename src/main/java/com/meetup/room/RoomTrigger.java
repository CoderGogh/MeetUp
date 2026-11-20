package com.meetup.room;

/** 상태 전이 이벤트. host면 방장만, 아니면 시스템만 요청 (RFP 8.4.1) */
public enum RoomTrigger {
    CALCULATE_CANDIDATES(true),
    START_VOTING(true),
    RESET_INPUT(true),
    CONFIRM(true),
    PARTICIPANT_CHANGED(false),
    EXPIRE(false);

    private final boolean host;

    RoomTrigger(boolean host) {
        this.host = host;
    }

    public boolean hostOnly() {
        return host;
    }
}
