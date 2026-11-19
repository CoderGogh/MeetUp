package com.meetup.place;

import java.util.Set;

/** 만남 후보지 */
public record Candidate(
        int id,
        String name,
        int stationId,
        AreaType areaType,
        Region region,
        Set<Theme> themes,
        boolean underground,
        double lat,
        double lng) {

    public boolean hasTheme(Theme theme) {
        return theme != null && themes.contains(theme);
    }
}
