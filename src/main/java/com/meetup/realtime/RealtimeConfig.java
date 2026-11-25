package com.meetup.realtime;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

/** 실시간 구성. 하트비트용 스케줄링 */
@Configuration
@EnableScheduling
@EnableConfigurationProperties(RealtimeProperties.class)
class RealtimeConfig {
}
