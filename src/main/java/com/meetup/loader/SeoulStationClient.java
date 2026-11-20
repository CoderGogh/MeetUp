package com.meetup.loader;

import java.util.List;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;

/** 서울시 역사마스터 정보 Open API. 인증키는 경로에 실리므로 오류 메시지에 남기지 않음 */
public class SeoulStationClient {

    private static final String PATH = "/{key}/json/subwayStationMaster/1/1000/";

    private final RestClient http;
    private final String key;

    public SeoulStationClient(RestClient http, String key) {
        this.http = http;
        this.key = key;
    }

    public List<MasterStation> fetch() {
        try {
            String body = http.get().uri(PATH, key).retrieve().body(String.class);
            return SeoulStationResponse.parse(body == null ? "" : body);
        } catch (RestClientResponseException e) {
            throw new IllegalStateException("서울시 API HTTP " + e.getStatusCode().value());
        } catch (RestClientException e) {
            throw new IllegalStateException("서울시 API 호출 실패 " + e.getClass().getSimpleName());
        }
    }
}
