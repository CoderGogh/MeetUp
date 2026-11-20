package com.meetup.room;

import java.util.List;

/** 전이 CAS 결과. status 1 성공, 0 다른 요청이 먼저 바꿈, -1 방 없음 */
record CasResult(int status, String state, long rev) {

    static CasResult of(List<?> raw) {
        return new CasResult(Integer.parseInt(raw.get(0).toString()), raw.get(1).toString(),
                Long.parseLong(raw.get(2).toString()));
    }

    boolean applied() {
        return status == 1;
    }
}
