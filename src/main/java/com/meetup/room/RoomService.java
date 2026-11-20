package com.meetup.room;

import com.meetup.common.ApiException;
import com.meetup.common.ErrorCode;
import com.meetup.common.Hashes;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
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

    // 참여자 토큰 → 참여자 ID. 없거나 틀리면 401
    String authenticate(String code, String participantToken) {
        if (participantToken == null || participantToken.isBlank()) {
            throw new ApiException(ErrorCode.UNAUTHORIZED);
        }
        return rooms.participantIdByToken(code, Hashes.sha256(participantToken))
                .orElseThrow(() -> new ApiException(ErrorCode.UNAUTHORIZED));
    }

    private void checkCapacity(int capacity) {
        if (capacity > props.maxParticipants()) {
            throw new ApiException(ErrorCode.INVALID_REQUEST, "정원은 2~" + props.maxParticipants() + "명입니다",
                    Map.of("fields", Map.of("capacity", "2~" + props.maxParticipants())));
        }
    }

    // 코드가 겹치면 새 코드로 다시
    private Room save(CreateRoomRequest req, Participant host, String hostToken, String participantToken,
            Instant now) {
        Duration keyTtl = props.ttl().plus(props.expiredGrace());
        for (int i = 0; i < CODE_ATTEMPTS; i++) {
            Room room = new Room(RoomCodes.generate(), req.theme(), req.capacity(), RoomState.COLLECTING, 1,
                    now, now.plus(props.ttl()), Hashes.sha256(hostToken));
            if (rooms.create(room, host, Hashes.sha256(participantToken), keyTtl)) {
                return room;
            }
        }
        throw new IllegalStateException("방 코드 생성 충돌");
    }
}
