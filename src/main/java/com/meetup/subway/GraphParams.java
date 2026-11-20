package com.meetup.subway;

/** 그래프 시간 보정값. ODsay 표본으로 맞춤 (docs/loader.md) */
public record GraphParams(
        double dwellMinutes,
        double transferWaitMinutes,
        double defaultTransferWalk,
        double initialWaitMinutes,
        double estimatedKmh,
        double detour) {

    // 정차 0.7분, 환승 대기 4분, 환승 보행 기본 3분, 첫 대기 0분, 추정 구간 42km/h·우회 1.2배
    public static final GraphParams DEFAULT = new GraphParams(0.7, 4, 3, 0, 42, 1.2);
}
