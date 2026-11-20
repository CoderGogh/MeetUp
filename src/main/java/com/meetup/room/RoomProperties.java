package com.meetup.room;

import java.time.Duration;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

/** 방 설정 (meetup.room.*). 만료 후 grace 동안은 키를 남겨 만료 안내에 씀 */
@ConfigurationProperties("meetup.room")
public record RoomProperties(
        @DefaultValue("PT6H") Duration ttl,
        @DefaultValue("PT24H") Duration confirmedTtl,
        @DefaultValue("10") int maxParticipants,
        @DefaultValue("PT1H") Duration expiredGrace,
        @DefaultValue("http://localhost:8080") String inviteBaseUrl) {
}
