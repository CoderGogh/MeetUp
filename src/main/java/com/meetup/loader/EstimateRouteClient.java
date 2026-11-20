package com.meetup.loader;

import com.meetup.place.Candidate;
import com.meetup.place.Station;

/** 키 없는 환경용 추정 (직선거리 기반, 환승 미추정) */
public class EstimateRouteClient implements RouteClient {

    static final double WALK_KM = 0.7;
    private static final double WALK_MIN_PER_KM = 15.0;
    private static final double DETOUR = 1.3;
    private static final double TRANSIT_KMH = 22.0;
    private static final int ACCESS_MIN = 6;

    @Override
    public Route find(Station from, Candidate to) {
        Route near = nearby(from, to);
        if (near != null) {
            return near;
        }
        double km = Geo.distanceKm(from.lat(), from.lng(), to.lat(), to.lng());
        int minutes = (int) Math.round(km * DETOUR / TRANSIT_KMH * 60) + ACCESS_MIN;
        return new Route(minutes, 0, Route.Source.ESTIMATE);
    }

    // 같은 역·도보 거리면 API 없이 산출
    static Route nearby(Station from, Candidate to) {
        if (from.id() == to.stationId()) {
            return new Route(0, 0, Route.Source.ESTIMATE);
        }
        double km = Geo.distanceKm(from.lat(), from.lng(), to.lat(), to.lng());
        if (km > WALK_KM) {
            return null;
        }
        return new Route((int) Math.ceil(km * WALK_MIN_PER_KM), 0, Route.Source.ESTIMATE);
    }
}
