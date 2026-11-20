package com.meetup.subway;

import com.meetup.place.Geo;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;

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

    public static SubwayGraph build(List<LineSegment> segments, Map<String, double[]> coords,
            TransferTimes transfers, GraphParams p) {
        SubwayGraph g = new SubwayGraph();
        for (LineSegment s : segments) {
            Double ride = s.minutes() != null ? s.minutes() : estimate(s, coords, p);
            if (ride == null) {
                g.skipped++;
                continue;
            }
            int a = g.node(s.from(), s.line());
            int b = g.node(s.to(), s.line());
            g.link(a, b, ride + p.dwellMinutes(), 0);
            g.link(b, a, ride + p.dwellMinutes(), 0);
        }
        g.linkTransfers(transfers, p);
        return g;
    }

    // 소요시간 없는 구간: 좌표 거리 × 우회 ÷ 속도. 좌표 없으면 null
    private static Double estimate(LineSegment s, Map<String, double[]> coords, GraphParams p) {
        double[] a = coords.get(s.from());
        double[] b = coords.get(s.to());
        if (a == null || b == null) {
            return null;
        }
        return Geo.distanceKm(a[0], a[1], b[0], b[1]) * p.detour() / p.estimatedKmh() * 60;
    }

    // 같은 역의 다른 노선끼리 환승 간선 (보행 + 대기)
    private void linkTransfers(TransferTimes transfers, GraphParams p) {
        for (var e : byStation.entrySet()) {
            for (int a : e.getValue()) {
                for (int b : e.getValue()) {
                    if (a != b) {
                        Double walk = transfers.walk(e.getKey(), lineOf.get(a), lineOf.get(b));
                        double w = walk != null ? walk : p.defaultTransferWalk();
                        link(a, b, w + p.transferWaitMinutes(), 1);
                    }
                }
            }
        }
    }

    /** 출발역 기준 최단 시간 (다익스트라) */
    public Map<String, Path> shortestFrom(String station, double initialWait) {
        double[] dist = new double[stationOf.size()];
        int[] hops = new int[dist.length];
        Arrays.fill(dist, Double.POSITIVE_INFINITY);
        PriorityQueue<double[]> pq = new PriorityQueue<>(Comparator.comparingDouble(q -> q[0]));
        for (int n : byStation.getOrDefault(station, List.of())) {
            dist[n] = initialWait;
            pq.add(new double[] {initialWait, n});
        }
        while (!pq.isEmpty()) {
            double[] cur = pq.poll();
            int u = (int) cur[1];
            if (cur[0] > dist[u]) {
                continue;
            }
            for (Edge e : adj.get(u)) {
                if (dist[u] + e.minutes() < dist[e.to()]) {
                    dist[e.to()] = dist[u] + e.minutes();
                    hops[e.to()] = hops[u] + e.transfers();
                    pq.add(new double[] {dist[e.to()], e.to()});
                }
            }
        }
        return best(station, dist, hops);
    }

    public boolean has(String station) {
        return byStation.containsKey(station);
    }

    // 좌표가 없어 못 넣은 구간 수
    public int skipped() {
        return skipped;
    }

    private int node(String station, String line) {
        return index.computeIfAbsent(station + ":" + line, k -> {
            stationOf.add(station);
            lineOf.add(line);
            adj.add(new ArrayList<>());
            byStation.computeIfAbsent(station, s -> new ArrayList<>()).add(stationOf.size() - 1);
            return stationOf.size() - 1;
        });
    }

    private void link(int a, int b, double minutes, int transfers) {
        adj.get(a).add(new Edge(b, minutes, transfers));
    }

    // 역별로 가장 빠른 노선 노드
    private Map<String, Path> best(String origin, double[] dist, int[] hops) {
        Map<String, Path> best = new HashMap<>();
        for (int n = 0; n < dist.length; n++) {
            if (dist[n] < Double.POSITIVE_INFINITY) {
                best.merge(stationOf.get(n), new Path(dist[n], hops[n]),
                        (x, y) -> x.minutes() <= y.minutes() ? x : y);
            }
        }
        best.put(origin, new Path(0, 0));
        return best;
    }

    /** 최단 경로 결과 */
    public record Path(double minutes, int transfers) {
    }

    private record Edge(int to, double minutes, int transfers) {
    }
}
