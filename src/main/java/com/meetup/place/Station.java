package com.meetup.place;

import java.util.List;

/** 기준 역 */
public record Station(int id, String name, List<String> lines, double lat, double lng) {
}
