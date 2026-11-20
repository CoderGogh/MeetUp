package com.meetup.room;

import java.time.Clock;
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
}
