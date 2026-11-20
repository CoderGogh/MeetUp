package com.meetup.room;

import java.time.Clock;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/** 방 구성 */
@Configuration
@EnableConfigurationProperties(RoomProperties.class)
class RoomConfig {

    @Bean
    Clock clock() {
        return Clock.systemUTC();
    }
}
