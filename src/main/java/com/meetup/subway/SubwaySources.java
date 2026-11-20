package com.meetup.subway;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import org.springframework.core.io.ClassPathResource;

/** 그래프 원본 (src/main/resources/subway) */
public final class SubwaySources {

    private SubwaySources() {
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
