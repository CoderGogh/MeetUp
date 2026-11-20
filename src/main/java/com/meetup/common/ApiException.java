package com.meetup.common;

import java.util.Map;

/** 오류 코드와 클라이언트용 상세값을 실은 예외 */
public class ApiException extends RuntimeException {

    private final ErrorCode code;
    private final Map<String, Object> details;

    public ApiException(ErrorCode code) {
        this(code, code.message(), Map.of());
    }

    public ApiException(ErrorCode code, Map<String, Object> details) {
        this(code, code.message(), details);
    }

    public ApiException(ErrorCode code, String message, Map<String, Object> details) {
        super(message);
        this.code = code;
        this.details = details;
    }

    public ErrorCode code() {
        return code;
    }

    public Map<String, Object> details() {
        return details;
    }
}
