package com.meetup.common;

import com.fasterxml.jackson.databind.JsonNode;

/** 요청 키별 저장 내용. 처리 중이면 status 0 */
record IdempotencyRecord(String fingerprint, int status, JsonNode body) {

    static IdempotencyRecord pending(String fingerprint) {
        return new IdempotencyRecord(fingerprint, 0, null);
    }

    boolean done() {
        return status > 0;
    }
}
