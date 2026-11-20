package com.meetup.loader;

import com.meetup.place.Candidate;
import com.meetup.place.Station;

/** 역 → 후보지 대중교통 경로 조회 */
public interface RouteClient {

    Route find(Station from, Candidate to);
}
