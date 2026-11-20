package com.meetup.loader;

/** 그래프 행렬 계산 결과. 오차는 ODsay 실측 대비 분 단위 */
public record GraphReport(int pairs, int stationsOutsideGraph, int skippedSegments,
        int samples, double meanAbsError, double maxAbsError) {
}
