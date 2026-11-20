package com.meetup.loader;

import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

class SeoulStationClientTest {

    final RestClient.Builder builder = RestClient.builder().baseUrl("http://openapi.seoul.go.kr:8088");
    final MockRestServiceServer server = MockRestServiceServer.bindTo(builder).build();
    final SeoulStationClient client = new SeoulStationClient(builder.build(), "dummy");
}
