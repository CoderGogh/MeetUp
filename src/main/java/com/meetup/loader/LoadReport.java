package com.meetup.loader;

/** 1회 실행 결과 */
public record LoadReport(int planned, int loaded, int failed, int remaining) {
}
