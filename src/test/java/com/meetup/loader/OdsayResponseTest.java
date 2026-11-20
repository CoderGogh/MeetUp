package com.meetup.loader;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class OdsayResponseTest {

    final ObjectMapper json = new ObjectMapper();

    Route parse(String body) throws Exception {
        return OdsayResponse.parse(json.readTree(body));
    }

    @Test
    void picksShortestPath() throws Exception {
        Route r = parse("""
                {"result":{"path":[
                  {"info":{"totalTime":42,"busTransitCount":0,"subwayTransitCount":2}},
                  {"info":{"totalTime":35,"busTransitCount":1,"subwayTransitCount":1}}]}}""");
        assertThat(r).isEqualTo(new Route(35, 1, Route.Source.ODSAY));
    }

    @Test
    void singleRideHasNoTransfer() throws Exception {
        Route r = parse("""
                {"result":{"path":[{"info":{"totalTime":20,"busTransitCount":0,"subwayTransitCount":1}}]}}""");
        assertThat(r.transfers()).isZero();
    }

    @Test
    void errorObjectThrows() {
        assertThatThrownBy(() -> parse("""
                {"error":{"code":"-98","msg":"출, 도착지가 700m이내입니다."}}"""))
                .isInstanceOf(RouteException.class).hasMessageContaining("-98");
    }

    @Test
    void errorArrayThrows() {
        assertThatThrownBy(() -> parse("""
                {"error":[{"code":"500","message":"ApiKey authentication failed."}]}"""))
                .isInstanceOf(RouteException.class).hasMessageContaining("500");
    }
}
