package com.meetup.loader;

import com.fasterxml.jackson.databind.JsonNode;
import com.meetup.place.Candidate;
import com.meetup.place.Station;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;

/** ODsay 대중교통 경로 조회. 인증키는 쿼리에만 싣고 오류 메시지에 남기지 않음 */
public class OdsayRouteClient implements RouteClient {

    private static final String PATH =
            "/v1/api/searchPubTransPathT?SX={sx}&SY={sy}&EX={ex}&EY={ey}&apiKey={key}";

    private final RestClient http;
    private final String apiKey;

    public OdsayRouteClient(RestClient http, String apiKey) {
        this.http = http;
        this.apiKey = apiKey;
    }

    @Override
    public Route find(Station from, Candidate to) {
        // 700m 이내는 ODsay 미지원
        Route near = EstimateRouteClient.nearby(from, to);
        if (near != null) {
            return near;
        }
        return OdsayResponse.parse(call(from, to));
    }

    private JsonNode call(Station from, Candidate to) {
        try {
            JsonNode body = http.get()
                    .uri(PATH, from.lng(), from.lat(), to.lng(), to.lat(), apiKey)
                    .retrieve()
                    .body(JsonNode.class);
            if (body == null) {
                throw new RouteException("ODsay 빈 응답");
            }
            return body;
        } catch (RestClientResponseException e) {
            throw new RouteException("ODsay HTTP " + e.getStatusCode().value());
        } catch (RestClientException e) {
            throw new RouteException("ODsay 호출 실패 " + e.getClass().getSimpleName());
        }
    }
}
