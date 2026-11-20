package com.meetup.room;

import com.meetup.common.ApiException;
import com.meetup.common.ErrorCode;
import java.util.HashMap;
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
}
