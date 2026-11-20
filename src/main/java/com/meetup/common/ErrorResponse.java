package com.meetup.common;

import java.util.Map;

/** 공통 오류 응답 본문 */
public record ErrorResponse(String code, String message, Map<String, Object> details) {

    public static ErrorResponse of(ErrorCode code, String message, Map<String, Object> details) {
        return new ErrorResponse(code.name(), message, details);
    }
}
