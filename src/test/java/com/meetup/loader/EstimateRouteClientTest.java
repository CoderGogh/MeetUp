package com.meetup.loader;

import com.meetup.place.AreaType;
import com.meetup.place.Candidate;
import com.meetup.place.Region;
import com.meetup.place.Station;
import com.meetup.place.Theme;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class EstimateRouteClientTest {

    final EstimateRouteClient client = new EstimateRouteClient();
    final Station gangnam = new Station(1, "강남", List.of("2호선"), 37.4979, 127.0276);

    static Candidate at(int stationId, double lat, double lng) {
        return new Candidate(9, "후보", stationId, AreaType.MEDIUM, Region.GANGNAM, Set.of(Theme.TALK), false, lat, lng);
    }

    @Test
    void sameStationIsZero() {
        assertThat(client.find(gangnam, at(1, 37.4979, 127.0276)))
                .isEqualTo(new Route(0, 0, Route.Source.ESTIMATE));
    }
}
