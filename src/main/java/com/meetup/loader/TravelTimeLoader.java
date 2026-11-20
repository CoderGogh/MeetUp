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
}
