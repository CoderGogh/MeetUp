package com.meetup.loader;

import com.meetup.place.Candidate;
import com.meetup.place.PlaceRepository;
import com.meetup.place.Station;
import java.time.Duration;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static org.assertj.core.api.Assertions.assertThat;

class TravelTimeLoaderTest extends LoaderDbTest {

    @Autowired
    PlaceRepository places;
    @Autowired
    LoadProgressRepository progress;
    @Autowired
    TravelTimeWriter writer;

    final FakeOdsay fake = new FakeOdsay();

    TravelTimeLoader loader(int dailyLimit) {
        var props = new LoaderProperties(true, dailyLimit, 3, 20, Duration.ZERO, null, null, null);
        return new TravelTimeLoader(places, progress, writer, fake, props);
    }

    /** 호출 기록용 가짜 ODsay */
    static class FakeOdsay implements RouteClient {

        final List<LoadKey> calls = new ArrayList<>();
        final Set<Integer> failing = new HashSet<>();

        @Override
        public Route find(Station from, Candidate to) {
            calls.add(new LoadKey(from.id(), to.id()));
            if (failing.contains(to.id())) {
                throw new RouteException("ODsay HTTP 500");
            }
            return new Route(30, 1, Route.Source.ODSAY);
        }
    }

    @Test
    void stopsAtDailyLimit() {
        LoadReport r = loader(50).run();
        assertThat(fake.calls).hasSize(50);
        assertThat(count("SELECT count(*) FROM travel_time")).isEqualTo(50);
        assertThat(r.remaining()).isEqualTo(pairs() - 50);
    }
}
