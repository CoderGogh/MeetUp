package com.meetup.loader;

import java.nio.file.Path;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

/** 기준역 적재(서울시 API·CSV) 후 이동시간 행렬 적재 (그래프 계산 또는 ODsay) */
@Component
@ConditionalOnProperty(name = "meetup.loader.enabled", havingValue = "true")
class LoaderRunner implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(LoaderRunner.class);

    private final SeoulStationImporter seoul;
    private final StationCsvImporter importer;
    private final GraphMatrixLoader graph;
    private final TravelTimeLoader loader;
    private final MonthlyCallBudget odsayBudget;
    private final LoaderProperties props;

    LoaderRunner(SeoulStationImporter seoul, StationCsvImporter importer, GraphMatrixLoader graph,
            TravelTimeLoader loader, MonthlyCallBudget odsayBudget, LoaderProperties props) {
        this.seoul = seoul;
        this.importer = importer;
        this.graph = graph;
        this.loader = loader;
        this.odsayBudget = odsayBudget;
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
        if (props.graphMode()) {
            GraphReport g = graph.run();
            log.info("그래프 행렬: 저장 {} · 그래프 밖 역 {} · 좌표 없는 구간 {} · ODsay 표본 {}건 평균 오차 {}분 최대 {}분",
                    g.pairs(), g.stationsOutsideGraph(), g.skippedSegments(), g.samples(),
                    String.format("%.1f", g.meanAbsError()), String.format("%.1f", g.maxAbsError()));
            return;
        }
        int left = odsayBudget.remaining();
        log.info("ODsay 이번 달 남은 호출 {}건 · 이번 실행 상한 {}건", left, props.dailyLimit());
        if (props.hasOdsayKey() && left == 0) {
            log.warn("ODsay 월 한도 소진. 다음 달에 다시 실행");
            return;
        }
        LoadReport r = loader.run();
        log.info("행렬 적재: 신규 조합 {} · 저장 {} · 실패 {} · 남음 {}",
                r.planned(), r.loaded(), r.failed(), r.remaining());
        if (r.quotaExceeded()) {
            log.warn("외부 API 호출 한도 초과로 중단. 한도가 풀린 뒤 다시 실행");
        }
    }
}
