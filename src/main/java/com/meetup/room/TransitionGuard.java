package com.meetup.room;

import com.meetup.common.ApiException;
import com.meetup.common.ErrorCode;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Component;

/** 전이 조건 (RFP 8.4.1). 안 되면 409 PRECONDITION_FAILED + 현재 상태·사유 */
@Component
class TransitionGuard {

    private final RoomRepository rooms;

    TransitionGuard(RoomRepository rooms) {
        this.rooms = rooms;
    }

    private static ApiException fail(String message, Map<String, Object> where, Map<String, Object> why) {
        Map<String, Object> details = new HashMap<>(where);
        details.putAll(why);
        return new ApiException(ErrorCode.PRECONDITION_FAILED, message, details);
    }

    // 참여자 2명 이상, 전원 출발지 입력
    private void candidatesReady(Room room, Map<String, Object> where) {
        List<Participant> ps = rooms.participants(room.code());
        if (ps.size() < 2) {
            throw fail("참여자가 2명 이상이어야 합니다", where, Map.of("reason", "MIN_PARTICIPANTS", "required", 2));
        }
        List<String> missing = ps.stream().filter(p -> !p.originSubmitted()).map(Participant::nickname).toList();
        if (!missing.isEmpty()) {
            throw fail("출발지를 입력하지 않은 참여자가 있습니다", where,
                    Map.of("reason", "ORIGINS_MISSING", "missing", missing));
        }
    }

    // 득표 1위가 유일하거나 추첨이 끝남
    private void leaderDecided(Room room, Map<String, Object> where) {
        if (!rooms.leaderDecided(room.code())) {
            throw fail("득표 1위가 정해지지 않았습니다", where, Map.of("reason", "NO_LEADER"));
        }
    }
}
