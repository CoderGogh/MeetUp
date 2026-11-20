package com.meetup.loader;

import com.meetup.subway.LineSegment;
import com.meetup.subway.StationNames;
import com.meetup.subway.SubwaySources;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

/** 역사마스터에서 그래프에 있는 역·노선만 골라 기준역으로 적재 */
public class SeoulStationImporter {

    private final SeoulStationClient client;
    private final StationWriter writer;

    public SeoulStationImporter(SeoulStationClient client, StationWriter writer) {
        this.client = client;
        this.writer = writer;
    }

    public int importStations() {
        Map<String, Set<String>> graph = graphLines();
        Map<String, StationRecord> picked = new TreeMap<>();
        for (MasterStation m : client.fetch()) {
            String name = StationNames.normalize(m.name());
            String line = StationNames.line(m.route());
            Set<String> lines = graph.get(name);
            if (line != null && lines != null && lines.contains(line)) {
                picked.putIfAbsent(name, new StationRecord(name, lines, m.lat(), m.lng()));
            }
        }
        writer.upsert(picked.values());
        return picked.size();
    }

    // 역명 → 그래프 노선
    static Map<String, Set<String>> graphLines() {
        Map<String, Set<String>> lines = new HashMap<>();
        for (LineSegment s : SubwaySources.all()) {
            lines.computeIfAbsent(s.from(), k -> new TreeSet<>()).add(s.line());
            lines.computeIfAbsent(s.to(), k -> new TreeSet<>()).add(s.line());
        }
        return lines;
    }
}
