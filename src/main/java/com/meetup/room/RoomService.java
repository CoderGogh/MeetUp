package com.meetup.room;

import com.meetup.common.ApiException;
import com.meetup.common.ErrorCode;
import java.time.Clock;
import java.util.Map;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/** 방 생성·조회 */
@Service
public class RoomService {

    private static final Logger log = LoggerFactory.getLogger(RoomService.class);
    private static final int CODE_ATTEMPTS = 3;

    private final RoomRepository rooms;
    private final RoomProperties props;
    private final Clock clock;

    public RoomService(RoomRepository rooms, RoomProperties props, Clock clock) {
        this.rooms = rooms;
        this.props = props;
        this.clock = clock;
    }

    String inviteUrl(String code) {
        return props.inviteBaseUrl() + "/r/" + code;
    }

    // 없는 방 404, 만료된 방 410
    Room load(String code) {
        Room room = (RoomCodes.isValid(code) ? rooms.find(code) : Optional.<Room>empty())
                .orElseThrow(() -> new ApiException(ErrorCode.ROOM_NOT_FOUND));
        if (room.stateAt(clock.instant()) == RoomState.EXPIRED) {
            throw new ApiException(ErrorCode.ROOM_EXPIRED, Map.of("expiredAt", room.expiresAt().toString()));
        }
        return room;
    }
}
