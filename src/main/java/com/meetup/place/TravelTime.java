package com.meetup.place;

/** 역 → 후보지 소요시간 */
public record TravelTime(int stationId, int candidateId, int minutes, int transfers) {
}
