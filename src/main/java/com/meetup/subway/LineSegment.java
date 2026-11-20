package com.meetup.subway;

/** 인접 두 역 구간. minutes가 null이면 좌표 거리로 추정 */
public record LineSegment(String line, String from, String to, Double minutes) {
}
