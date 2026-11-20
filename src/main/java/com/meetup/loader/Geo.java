package com.meetup.loader;

/** 좌표 간 직선거리 */
final class Geo {

    private static final double EARTH_KM = 6371.0;

    private Geo() {
    }

    static double distanceKm(double lat1, double lng1, double lat2, double lng2) {
        double dLat = Math.toRadians(lat2 - lat1);
        double dLng = Math.toRadians(lng2 - lng1);
        double a = Math.pow(Math.sin(dLat / 2), 2) + Math.cos(Math.toRadians(lat1))
                * Math.cos(Math.toRadians(lat2)) * Math.pow(Math.sin(dLng / 2), 2);
        return 2 * EARTH_KM * Math.asin(Math.sqrt(a));
    }
}
