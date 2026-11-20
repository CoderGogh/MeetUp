package com.meetup.loader;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

/** 이동시간 행렬 적재 실행 */
@Component
@ConditionalOnProperty(name = "meetup.loader.enabled", havingValue = "true")
class LoaderRunner implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(LoaderRunner.class);

    private final TravelTimeLoader loader;
    private final LoaderProperties props;

    LoaderRunner(TravelTimeLoader loader, LoaderProperties props) {
        this.loader = loader;
        this.props = props;
    }

    @Override
    public void run(ApplicationArguments args) throws Exception {
        LoadReport r = loader.run();
        log.info("행렬 적재: 신규 조합 {} · 저장 {} · 실패 {} · 남음 {}",
                r.planned(), r.loaded(), r.failed(), r.remaining());
    }
}
