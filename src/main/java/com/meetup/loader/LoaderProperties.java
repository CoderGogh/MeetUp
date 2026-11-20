package com.meetup.loader;

import java.time.Duration;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

/** 적재 설정 (meetup.loader.*) */
@ConfigurationProperties("meetup.loader")
public record LoaderProperties(
        @DefaultValue("false") boolean enabled,
        @DefaultValue("0") int dailyLimit,
        @DefaultValue("3") int maxAttempts,
        @DefaultValue("20") int batchSize,
        @DefaultValue("200ms") Duration delay,
        Duration refreshOlderThan,
        String stationsCsv,
        String odsayKey,
        String seoulKey,
        @DefaultValue("graph") String mode,
        @DefaultValue("30") int odsayMonthlyLimit) {

    public boolean hasOdsayKey() {
        return odsayKey != null && !odsayKey.isBlank();
    }

    // graph: 지하철 그래프 계산, odsay: ODsay 호출 적재
    public boolean graphMode() {
        return !"odsay".equalsIgnoreCase(mode);
    }

    public boolean hasSeoulKey() {
        return seoulKey != null && !seoulKey.isBlank();
    }
}
