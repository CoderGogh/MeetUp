package com.meetup.loader;

import java.util.Set;

/** 적재할 기준역 (노선 병합 후) */
public record StationRecord(String name, Set<String> lines, double lat, double lng) {
}
