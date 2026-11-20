package com.meetup.loader;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

/** 적재 구성. 키가 없으면 추정 모드 */
@Configuration
@EnableConfigurationProperties(LoaderProperties.class)
class LoaderConfig {

    private static final Logger log = LoggerFactory.getLogger(LoaderConfig.class);

    @Bean
    RouteClient routeClient(LoaderProperties props, RestClient.Builder builder) {
        if (!props.hasOdsayKey()) {
            log.info("ODsay 키 없음 → 추정 모드 (source=ESTIMATE)");
            return new EstimateRouteClient();
        }
        return new OdsayRouteClient(builder.baseUrl("https://api.odsay.com").build(), props.odsayKey());
    }

}
