package com.meetup.loader;

import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withServerError;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

class SeoulStationClientTest {

    final RestClient.Builder builder = RestClient.builder().baseUrl("http://openapi.seoul.go.kr:8088");
    final MockRestServiceServer server = MockRestServiceServer.bindTo(builder).build();
    final SeoulStationClient client = new SeoulStationClient(builder.build(), "dummy");

    @Test
    void requestsAllRowsWithKeyInPath() {
        server.expect(requestTo("http://openapi.seoul.go.kr:8088/dummy/json/subwayStationMaster/1/1000/"))
                .andRespond(withSuccess("""
                        {"subwayStationMaster":{"RESULT":{"CODE":"INFO-000"},"row":[]}}""",
                        MediaType.APPLICATION_JSON));
        assertThat(client.fetch()).isEmpty();
        server.verify();
    }

    @Test
    void httpErrorHidesKey() {
        server.expect(requestTo(org.hamcrest.Matchers.startsWith("http://openapi.seoul.go.kr:8088")))
                .andRespond(withServerError());
        assertThatThrownBy(client::fetch)
                .hasMessageContaining("500")
                .satisfies(e -> assertThat(e.getMessage()).doesNotContain("dummy"));
    }
}
