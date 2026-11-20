package com.meetup.loader;

import java.io.IOException;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
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
}
