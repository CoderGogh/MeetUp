package com.meetup.loader;

import com.meetup.place.Station;
import java.util.List;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

class OdsayRouteClientTest {

    static final String KEY = "secret+key/123";

    final RestClient.Builder builder = RestClient.builder().baseUrl("https://api.odsay.com");
    final MockRestServiceServer server = MockRestServiceServer.bindTo(builder).build();
    final OdsayRouteClient client = new OdsayRouteClient(builder.build(), KEY);
    final Station gangnam = new Station(1, "강남", List.of("2호선"), 37.4979, 127.0276);
}
