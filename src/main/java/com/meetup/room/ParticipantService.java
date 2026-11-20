package com.meetup.room;

import com.meetup.common.ApiException;
import com.meetup.common.ErrorCode;
import java.time.Clock;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

/** 입장·별명·참여자 토큰 (FR-03) */
@Service
public class ParticipantService {

    private static final Logger log = LoggerFactory.getLogger(ParticipantService.class);

    private final RoomService rooms;
    private final RoomRepository repo;
    private final ApplicationEventPublisher events;
    private final Clock clock;

    public ParticipantService(RoomService rooms, RoomRepository repo, ApplicationEventPublisher events,
            Clock clock) {
        this.rooms = rooms;
        this.repo = repo;
        this.events = events;
        this.clock = clock;
    }

    // 스크립트 결과 → 오류 응답
    private static ApiException reject(JoinResult r) {
        return switch (r.outcome()) {
            case "TAKEN" -> new ApiException(ErrorCode.NICKNAME_TAKEN);
            case "FULL" -> new ApiException(ErrorCode.ROOM_FULL, Map.of("capacity", Integer.parseInt(r.value())));
            case "STATE" -> new ApiException(ErrorCode.INVALID_TRANSITION, "지금 단계에서는 입장할 수 없습니다",
                    Map.of("currentState", r.value()));
            case "EXPIRED" -> new ApiException(ErrorCode.ROOM_EXPIRED);
            default -> new ApiException(ErrorCode.ROOM_NOT_FOUND);
        };
    }
}
