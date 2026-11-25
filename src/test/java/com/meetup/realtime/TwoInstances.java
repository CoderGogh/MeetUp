package com.meetup.realtime;

import com.meetup.MeetupApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.servlet.context.ServletWebServerApplicationContext;
import org.springframework.context.ConfigurableApplicationContext;

/** 같은 Redis를 쓰는 두 번째 인스턴스 (테스트용) */
final class TwoInstances {

    private TwoInstances() {
    }

    static ConfigurableApplicationContext start(String broadcast) {
        return new SpringApplicationBuilder(MeetupApplication.class).profiles("test")
                .properties("server.port=0", "meetup.realtime.broadcast=" + broadcast).run();
    }

    static int port(ConfigurableApplicationContext ctx) {
        return ((ServletWebServerApplicationContext) ctx).getWebServer().getPort();
    }
}
