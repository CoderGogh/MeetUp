package com.meetup.loader;

import java.nio.file.Path;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

/** 기준역 적재(서울시 API·CSV) 후 이동시간 행렬 적재 */
@Component
@ConditionalOnProperty(name = "meetup.loader.enabled", havingValue = "true")
class LoaderRunner implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(LoaderRunner.class);

    private final SeoulStationImporter seoul;
    private final StationCsvImporter importer;
    private final TravelTimeLoader loader;
    private final LoaderProperties props;

    LoaderRunner(SeoulStationImporter seoul, StationCsvImporter importer, TravelTimeLoader loader,
            LoaderProperties props) {
        this.seoul = seoul;
        this.importer = importer;
        this.loader = loader;
        this.props = props;
    }

    @Override
    public void run(ApplicationArguments args) throws Exception {
        if (props.hasSeoulKey()) {
            log.info("기준역 {}개 적재 (서울시 역사마스터)", seoul.importStations());
        }
        if (props.stationsCsv() != null && !props.stationsCsv().isBlank()) {
            log.info("기준역 {}개 적재", importer.importFrom(Path.of(props.stationsCsv())));
        }
        LoadReport r = loader.run();
        log.info("행렬 적재: 신규 조합 {} · 저장 {} · 실패 {} · 남음 {}",
                r.planned(), r.loaded(), r.failed(), r.remaining());
    }
}
