package com.meetup.subway;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/** 역 × 노선을 노드로, 승차 구간과 환승을 간선으로 둔 그래프 */
public final class SubwayGraph {

    private final Map<String, Integer> index = new HashMap<>();
    private final Map<String, List<Integer>> byStation = new HashMap<>();
    private final List<String> stationOf = new ArrayList<>();
    private final List<String> lineOf = new ArrayList<>();
    private final List<List<Edge>> adj = new ArrayList<>();
    private int skipped;

    private SubwayGraph() {
    }

    private void link(int a, int b, double minutes, int transfers) {
        adj.get(a).add(new Edge(b, minutes, transfers));
    }

    private record Edge(int to, double minutes, int transfers) {
    }
}
