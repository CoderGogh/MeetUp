package com.meetup.realtime;

import java.time.Duration;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

/** 실시간 설정 (meetup.realtime.*). 연결은 timeout마다 끊기고 클라이언트가 다시 붙음 */
@ConfigurationProperties("meetup.realtime")
public record RealtimeProperties(
        @DefaultValue("PT30M") Duration emitterTimeout,
        @DefaultValue("PT20S") Duration heartbeat) {
}
