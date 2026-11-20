package com.meetup.loader;

import java.io.IOException;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

/** 기준역 CSV 적재. 한 행 = 역 × 노선, 같은 역명은 노선을 합쳐 1행 */
@Component
public class StationCsvImporter {

    private static final Charset MS949 = Charset.forName("MS949");

    private final JdbcTemplate jdbc;

    public StationCsvImporter(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    // 공공데이터 CSV는 MS949 인코딩이 많음
    private static List<String> read(Path csv) throws IOException {
        try {
            return Files.readAllLines(csv, StandardCharsets.UTF_8);
        } catch (CharacterCodingException e) {
            return Files.readAllLines(csv, MS949);
        }
    }

    private static String[] split(String line) {
        String[] f = line.replace("﻿", "").split(",", -1);
        for (int i = 0; i < f.length; i++) {
            f[i] = f[i].trim().replaceAll("^\"|\"$", "");
        }
        return f;
    }

    // 역명 기준 갱신 (후보 기준역 좌표 포함)
    private void upsert(Map<String, Row> rows) {
        jdbc.batchUpdate("INSERT INTO station (name, lines, geom) "
                + "VALUES (?, ?, ST_SetSRID(ST_MakePoint(?, ?), 4326)) "
                + "ON CONFLICT (name) DO UPDATE SET lines = EXCLUDED.lines, geom = EXCLUDED.geom",
                List.copyOf(rows.entrySet()), rows.size(), (ps, e) -> {
                    ps.setString(1, e.getKey());
                    ps.setString(2, String.join(",", e.getValue().lines));
                    ps.setDouble(3, e.getValue().lng);
                    ps.setDouble(4, e.getValue().lat);
                });
    }

    /** 헤더 위치 (영문·공공데이터 한글 헤더 모두 허용) */
    private record Columns(int name, int line, int lat, int lng) {

        static Columns of(String[] header) {
            List<String> h = List.of(header);
            return new Columns(find(h, "name", "역사명", "역명"), find(h, "line", "호선", "노선명"),
                    find(h, "lat", "위도"), find(h, "lng", "경도"));
        }

        private static int find(List<String> header, String... names) {
            for (String n : names) {
                int i = header.indexOf(n);
                if (i >= 0) {
                    return i;
                }
            }
            throw new IllegalArgumentException("CSV 헤더 없음: " + names[0]);
        }
    }

    // 01호선 → 1호선
    private static String normalizeLine(String line) {
        return line.replaceFirst("^0(\\d)", "$1");
    }

    /** 역 하나 (노선 병합) */
    private static final class Row {

        final double lat;
        final double lng;
        final Set<String> lines = new LinkedHashSet<>();

        Row(double lat, double lng) {
            this.lat = lat;
            this.lng = lng;
        }
    }
}
