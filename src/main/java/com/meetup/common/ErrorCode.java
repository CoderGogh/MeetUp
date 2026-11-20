package com.meetup.common;

import org.springframework.http.HttpStatus;

/** 공통 오류 코드 (응답 code 필드) */
public enum ErrorCode {
    INVALID_REQUEST(HttpStatus.BAD_REQUEST, "요청 형식이 올바르지 않습니다"),
    UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "참여자 인증이 필요합니다"),
    FORBIDDEN(HttpStatus.FORBIDDEN, "권한이 없습니다"),
    NOT_FOUND(HttpStatus.NOT_FOUND, "요청한 경로가 없습니다"),
    METHOD_NOT_ALLOWED(HttpStatus.METHOD_NOT_ALLOWED, "지원하지 않는 메서드입니다"),
    ROOM_NOT_FOUND(HttpStatus.NOT_FOUND, "방을 찾을 수 없습니다"),
    ROOM_EXPIRED(HttpStatus.GONE, "만료된 방입니다. 새 방을 만들어 주세요"),
    NICKNAME_TAKEN(HttpStatus.CONFLICT, "이미 사용 중인 별명입니다"),
    ROOM_FULL(HttpStatus.CONFLICT, "정원이 찼습니다"),
    INVALID_TRANSITION(HttpStatus.CONFLICT, "지금 상태에서는 할 수 없는 요청입니다"),
    PRECONDITION_FAILED(HttpStatus.CONFLICT, "다음 단계로 넘어갈 조건이 안 됩니다"),
    IDEMPOTENCY_IN_PROGRESS(HttpStatus.CONFLICT, "같은 요청을 처리 중입니다"),
    IDEMPOTENCY_KEY_REUSED(HttpStatus.UNPROCESSABLE_ENTITY, "다른 요청에 쓴 요청 키입니다"),
    INTERNAL(HttpStatus.INTERNAL_SERVER_ERROR, "서버 오류가 발생했습니다");

    private final HttpStatus status;
    private final String message;

    ErrorCode(HttpStatus status, String message) {
        this.status = status;
        this.message = message;
    }

    public HttpStatus status() {
        return status;
    }

    public String message() {
        return message;
    }
}
