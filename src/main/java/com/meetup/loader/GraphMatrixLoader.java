package com.meetup.loader;

import com.meetup.loader.OdsaySampleRepository.Sample;
import com.meetup.loader.TravelTimeWriter.LoadedRoute;
import com.meetup.place.Candidate;
import com.meetup.place.PlaceRepository;
import com.meetup.place.Station;
import com.meetup.subway.GraphParams;
import com.meetup.subway.SubwayGraph;
import com.meetup.subway.SubwayGraph.Path;
import com.meetup.subway.SubwaySources;
import com.meetup.subway.TransferTimes;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/** 지하철 그래프로 역 × 후보지 행렬 계산. 외부 호출 없음 */
public class GraphMatrixLoader {

    private final PlaceRepository places;
    private final TravelTimeWriter writer;
    private final OdsaySampleRepository samples;
    private final GraphParams params;

    public GraphMatrixLoader(PlaceRepository places, TravelTimeWriter writer,
            OdsaySampleRepository samples, GraphParams params) {
        this.places = places;
        this.writer = writer;
        this.samples = samples;
        this.params = params;
    }

    // ODsay 실측 대비 절대 오차
    private GraphReport report(int pairs, int outside, int skipped, Map<LoadKey, Double> computed) {
        double sum = 0;
        double max = 0;
        int n = 0;
        for (Sample s : samples.findAll()) {
            Double g = computed.get(s.key());
            if (g != null) {
                double err = Math.abs(g - s.minutes());
                sum += err;
                max = Math.max(max, err);
                n++;
            }
        }
        return new GraphReport(pairs, outside, skipped, n, n == 0 ? 0 : sum / n, max);
    }
}
