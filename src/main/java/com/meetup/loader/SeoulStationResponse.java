package com.meetup.loader;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

/** 역사마스터 응답 해석 */
final class SeoulStationResponse {

    private static final ObjectMapper JSON = new ObjectMapper();

    private SeoulStationResponse() {
    }

    private static JsonNode read(String body) {
        try {
            return JSON.readTree(body);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("서울시 API 응답 형식 오류");
        }
    }
}
