package com.meetup.loader;

import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.beans.factory.annotation.Autowired;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class StationCsvImporterTest extends LoaderDbTest {

    @Autowired
    StationCsvImporter importer;

    @TempDir
    Path dir;

    Path csv(String body, Charset charset) throws Exception {
        return Files.write(dir.resolve("stations.csv"), body.getBytes(charset));
    }

    String lines(String name) {
        return jdbc.queryForObject("SELECT lines FROM station WHERE name = ?", String.class, name);
    }

    @Test
    void mergesLinesOfSameStation() throws Exception {
        Path f = csv("역사명,호선,위도,경도\n테스트역,01호선,37.5,127.0\n테스트역,2호선,37.5,127.0\n",
                Charset.forName("UTF-8"));
        assertThat(importer.importFrom(f)).isEqualTo(1);
        assertThat(lines("테스트역")).isEqualTo("1호선,2호선");
    }
}
