package com.meetup.loader;

import com.meetup.place.Candidate;
import com.meetup.place.Station;

/** 키 없는 환경용 추정 (직선거리 기반, 환승 미추정) */
public class EstimateRouteClient implements RouteClient {

    private static final double DETOUR = 1.3;
    private static final double TRANSIT_KMH = 22.0;
    private static final int ACCESS_MIN = 6;

    @Override
    public Route find(Station from, Candidate to) {
        double km = Geo.distanceKm(from.lat(), from.lng(), to.lat(), to.lng());
        int minutes = (int) Math.round(km * DETOUR / TRANSIT_KMH * 60) + ACCESS_MIN;
        return new Route(minutes, 0, Route.Source.ESTIMATE);
    }
}
