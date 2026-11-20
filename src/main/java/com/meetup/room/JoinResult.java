package com.meetup.room;

import java.util.List;

/** 입장 스크립트 결과 */
record JoinResult(String outcome, String value, RoomState stateBefore) {

    static JoinResult of(List<?> raw) {
        String before = raw.get(2).toString();
        return new JoinResult(raw.get(0).toString(), raw.get(1).toString(),
                before.isEmpty() ? null : RoomState.valueOf(before));
    }

    boolean ok() {
        return "OK".equals(outcome);
    }
}
