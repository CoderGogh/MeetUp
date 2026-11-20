package com.meetup.loader;

import com.meetup.loader.TravelTimeWriter.LoadedRoute;
import com.meetup.place.Candidate;
import com.meetup.place.PlaceRepository;
import com.meetup.place.Station;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/** 이동시간 행렬 적재. 진행 기록 기준으로 중단 지점부터 재개 */
public class TravelTimeLoader {

    private final PlaceRepository places;
    private final LoadProgressRepository progress;
    private final TravelTimeWriter writer;
    private final RouteClient client;
    private final LoaderProperties props;

    public TravelTimeLoader(PlaceRepository places, LoadProgressRepository progress,
            TravelTimeWriter writer, RouteClient client, LoaderProperties props) {
        this.places = places;
        this.progress = progress;
        this.writer = writer;
        this.client = client;
        this.props = props;
    }

    public LoadReport run() {
        int planned = progress.plan();
        if (props.refreshOlderThan() != null) {
            progress.requeueStale(props.refreshOlderThan());
        }
        Tally t = new Tally(byId(places.findStations(), Station::id),
                byId(places.findCandidates(), Candidate::id));
        while (t.calls < props.dailyLimit()) {
            List<LoadKey> batch = progress.next(
                    Math.min(props.batchSize(), props.dailyLimit() - t.calls), props.maxAttempts());
            if (batch.isEmpty()) {
                break;
            }
            load(batch, t);
        }
        return new LoadReport(planned, t.loaded, t.failed, progress.remaining(props.maxAttempts()));
    }

    // 배치 조회 후 한 번에 저장
    private void load(List<LoadKey> batch, Tally t) {
        List<LoadedRoute> ok = new ArrayList<>();
        for (LoadKey key : batch) {
            try {
                Route route = client.find(t.stations.get(key.stationId()), t.candidates.get(key.candidateId()));
                ok.add(new LoadedRoute(key, route));
                if (route.source() == Route.Source.ODSAY) {
                    t.calls++;
                }
            } catch (RouteException e) {
                // 실패 조합은 기록만 하고 다음 조합 진행
                progress.markFailed(key, e.getMessage());
                t.failed++;
                t.calls++;
            }
        }
        writer.save(ok);
        t.loaded += ok.size();
    }

    private static <T> Map<Integer, T> byId(List<T> items, Function<T, Integer> id) {
        return items.stream().collect(Collectors.toMap(id, Function.identity()));
    }

    /** 실행 중 집계 */
    private static final class Tally {

        final Map<Integer, Station> stations;
        final Map<Integer, Candidate> candidates;
        int calls;
        int loaded;
        int failed;

        Tally(Map<Integer, Station> stations, Map<Integer, Candidate> candidates) {
            this.stations = stations;
            this.candidates = candidates;
        }
    }
}
