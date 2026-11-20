package com.meetup.loader;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.ArrayList;
import java.util.List;

/** 역사마스터 응답 해석 */
final class SeoulStationResponse {

    private static final ObjectMapper JSON = new ObjectMapper();

    private SeoulStationResponse() {
    }

    static List<MasterStation> parse(String body) {
        JsonNode root = read(body).path("subwayStationMaster");
        String code = root.path("RESULT").path("CODE").asText("?");
        if (!"INFO-000".equals(code)) {
            throw new IllegalStateException("서울시 API 오류 코드 " + code);
        }
        List<MasterStation> rows = new ArrayList<>();
        for (JsonNode r : root.path("row")) {
            rows.add(new MasterStation(r.path("BLDN_NM").asText(), r.path("ROUTE").asText(),
                    r.path("LAT").asDouble(), r.path("LOT").asDouble()));
        }
        return rows;
    }

    private static JsonNode read(String body) {
        try {
            return JSON.readTree(body);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("서울시 API 응답 형식 오류");
        }
    }
}
