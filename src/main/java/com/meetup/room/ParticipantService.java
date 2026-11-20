package com.meetup.room;

import java.time.Clock;
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
}
