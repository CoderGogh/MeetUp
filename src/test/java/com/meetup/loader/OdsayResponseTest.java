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
}
