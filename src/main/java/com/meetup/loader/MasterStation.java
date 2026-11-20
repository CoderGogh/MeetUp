package com.meetup.loader;

/** 역사마스터 정보 한 행 (원본 역명·노선명 그대로) */
public record MasterStation(String name, String route, double lat, double lng) {
}
