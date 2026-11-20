package com.meetup.loader;

import java.time.Duration;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

/** 적재 설정 (meetup.loader.*) */
@ConfigurationProperties("meetup.loader")
public record LoaderProperties(
        @DefaultValue("false") boolean enabled,
        @DefaultValue("1000") int dailyLimit,
        @DefaultValue("3") int maxAttempts,
        @DefaultValue("20") int batchSize,
        @DefaultValue("200ms") Duration delay,
        Duration refreshOlderThan,
        String stationsCsv,
        String odsayKey) {

    public boolean hasOdsayKey() {
        return odsayKey != null && !odsayKey.isBlank();
    }
}
