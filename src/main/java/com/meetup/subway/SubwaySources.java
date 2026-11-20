package com.meetup.subway;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.springframework.core.io.ClassPathResource;

/** 그래프 원본 (src/main/resources/subway) */
public final class SubwaySources {

    private SubwaySources() {
    }

    // 지선 첫 역 → 갈라지는 역 (원본은 본선 뒤에 이어 적음)
    private static final Map<String, String> BRANCH_FROM = Map.of(
            "2호선:용답", "성수",
            "2호선:도림천", "신도림",
            "5호선:둔촌동", "강동");

    /** 서울교통공사 1~8호선 구간 (연번·호선·역명·소요시간 순) */
    public static List<LineSegment> seoulMetro() {
        List<LineSegment> segments = new ArrayList<>();
        String line = null;
        String prev = null;
        for (String[] r : rows("seoul_metro_interstation.csv")) {
            String l = StationNames.line(r[1]);
            String name = StationNames.normalize(r[2]);
            if (!l.equals(line)) {
                line = l;
                prev = null;
            }
            String from = BRANCH_FROM.getOrDefault(l + ":" + name, prev);
            if (from != null) {
                segments.add(new LineSegment(l, from, name, minutes(r[3])));
            }
            prev = name;
        }
        return segments;
    }

    /** 직접 정리한 노선 (노선·구간·역명, 연속 행이 한 구간) */
    public static List<LineSegment> extraLines() {
        List<LineSegment> segments = new ArrayList<>();
        String[] prev = null;
        for (String[] r : rows("extra_lines.csv")) {
            if (prev != null && prev[0].equals(r[0]) && prev[1].equals(r[1])) {
                segments.add(new LineSegment(r[0], StationNames.normalize(prev[2]),
                        StationNames.normalize(r[2]), null));
            }
            prev = r;
        }
        return segments;
    }

    public static List<LineSegment> all() {
        List<LineSegment> all = new ArrayList<>(seoulMetro());
        all.addAll(extraLines());
        return all;
    }

    // 헤더 뺀 행
    static List<String[]> rows(String file) {
        try (var in = new ClassPathResource("subway/" + file).getInputStream()) {
            List<String[]> rows = new ArrayList<>();
            for (String line : new String(in.readAllBytes(), StandardCharsets.UTF_8).split("\n")) {
                if (!line.isBlank()) {
                    rows.add(line.split(",", -1));
                }
            }
            return rows.subList(1, rows.size());
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    // mm:ss → 분
    static double minutes(String mmss) {
        String[] p = mmss.trim().split(":");
        return Integer.parseInt(p[0]) + Integer.parseInt(p[1]) / 60.0;
    }
}
