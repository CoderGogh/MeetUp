package com.meetup.loader;

import com.meetup.place.Station;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

import static com.meetup.loader.EstimateRouteClientTest.at;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.hamcrest.Matchers.startsWith;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.queryParam;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withServerError;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

class OdsayRouteClientTest {

    static final String KEY = "dummy+/";

    final RestClient.Builder builder = RestClient.builder().baseUrl("https://api.odsay.com");
    final MockRestServiceServer server = MockRestServiceServer.bindTo(builder).build();
    final OdsayRouteClient client = new OdsayRouteClient(builder.build(), KEY);
    final Station gangnam = new Station(1, "강남", List.of("2호선"), 37.4979, 127.0276);

    @Test
    void sendsCoordinatesAndKey() {
        server.expect(requestTo(startsWith("https://api.odsay.com/v1/api/searchPubTransPathT")))
                .andExpect(queryParam("SX", "127.0276"))
                .andExpect(queryParam("EY", "37.5133"))
                .andExpect(queryParam("apiKey", "dummy%2B%2F"))
                .andRespond(withSuccess("""
                        {"result":{"path":[{"info":{"totalTime":25,"subwayTransitCount":1}}]}}""",
                        MediaType.APPLICATION_JSON));
        assertThat(client.find(gangnam, at(2, 37.5133, 127.1001)).minutes()).isEqualTo(25);
        server.verify();
    }

    @Test
    void httpErrorHidesKey() {
        server.expect(requestTo(startsWith("https://api.odsay.com"))).andRespond(withServerError());
        assertThatThrownBy(() -> client.find(gangnam, at(2, 37.5133, 127.1001)))
                .isInstanceOf(RouteException.class)
                .hasMessageContaining("500")
                .satisfies(e -> assertThat(e.getMessage()).doesNotContain("dummy"));
    }

    @Test
    void nearbySkipsCall() {
        Route r = client.find(gangnam, at(1, 37.4979, 127.0276));
        assertThat(r.source()).isEqualTo(Route.Source.ESTIMATE);
        server.verify();
    }

    @Test
    void http429IsQuotaExceeded() {
        server.expect(requestTo(startsWith("https://api.odsay.com")))
                .andRespond(withStatus(HttpStatus.TOO_MANY_REQUESTS));
        assertThatThrownBy(() -> client.find(gangnam, at(2, 37.5133, 127.1001)))
                .isInstanceOf(QuotaExceededException.class);
    }
}
