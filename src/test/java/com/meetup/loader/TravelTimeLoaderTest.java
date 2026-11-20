package com.meetup.loader;

import com.meetup.place.Candidate;
import com.meetup.place.Station;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

class TravelTimeLoaderTest extends LoaderDbTest {

    final FakeOdsay fake = new FakeOdsay();

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
}
