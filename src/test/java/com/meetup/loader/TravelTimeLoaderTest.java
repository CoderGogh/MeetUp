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
        var props = new LoaderProperties(true, dailyLimit, 3, 20, Duration.ZERO, null, null, null, null, "odsay");
        return new TravelTimeLoader(places, progress, writer, fake, props);
    }

    /** 호출 기록용 가짜 ODsay */
    static class FakeOdsay implements RouteClient {

        final List<LoadKey> calls = new ArrayList<>();
        final Set<Integer> failing = new HashSet<>();
        int quotaAfter = Integer.MAX_VALUE;

        @Override
        public Route find(Station from, Candidate to) {
            if (calls.size() >= quotaAfter) {
                throw new QuotaExceededException("ODsay 호출 한도 초과");
            }
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

    @Test
    void resumesWithoutRepeating() {
        loader(50).run();
        loader(50).run();
        assertThat(fake.calls).hasSize(100).doesNotHaveDuplicates();
    }

    @Test
    void retriesFailedUpToMaxAttempts() {
        int bad = places.findCandidates().get(0).id();
        fake.failing.add(bad);
        LoadReport r = loader(100_000).run();
        int stations = count("SELECT count(*) FROM station");
        assertThat(r.failed()).isEqualTo(stations * 3);
        assertThat(count("SELECT count(*) FROM load_progress WHERE status = 'FAILED' AND attempts = 3 "
                + "AND last_error = 'ODsay HTTP 500'")).isEqualTo(stations);
        assertThat(r.remaining()).isZero();
    }

    @Test
    void storesSource() {
        loader(5).run();
        assertThat(jdbc.queryForList("SELECT DISTINCT source FROM travel_time", String.class))
                .containsExactly("ODSAY");
    }

    @Test
    void stopsOnQuotaWithoutFailingRest() {
        fake.quotaAfter = 30;
        LoadReport r = loader(1000).run();
        assertThat(r.quotaExceeded()).isTrue();
        assertThat(r.loaded()).isEqualTo(30);
        assertThat(count("SELECT count(*) FROM load_progress WHERE status = 'FAILED'")).isZero();
        assertThat(r.remaining()).isEqualTo(pairs() - 30);
    }
}
