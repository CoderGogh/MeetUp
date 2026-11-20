package com.meetup.common;

import jakarta.servlet.ServletException;
import java.util.LinkedHashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingRequestHeaderException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/** 모든 오류를 공통 응답 형식으로 */
@RestControllerAdvice
public class ApiExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(ApiExceptionHandler.class);

    private static ResponseEntity<ErrorResponse> respond(ErrorCode code, String message, Map<String, Object> details) {
        return ResponseEntity.status(code.status()).body(ErrorResponse.of(code, message, details));
    }

    @ExceptionHandler(ApiException.class)
    ResponseEntity<ErrorResponse> api(ApiException e) {
        return respond(e.code(), e.getMessage(), e.details());
    }

    // 필드별 검증 메시지
    @ExceptionHandler(MethodArgumentNotValidException.class)
    ResponseEntity<ErrorResponse> invalid(MethodArgumentNotValidException e) {
        Map<String, Object> fields = new LinkedHashMap<>();
        e.getBindingResult().getFieldErrors().forEach(f -> fields.put(f.getField(), f.getDefaultMessage()));
        return respond(ErrorCode.INVALID_REQUEST, ErrorCode.INVALID_REQUEST.message(), Map.of("fields", fields));
    }

    @ExceptionHandler(MissingRequestHeaderException.class)
    ResponseEntity<ErrorResponse> missingHeader(MissingRequestHeaderException e) {
        return respond(ErrorCode.INVALID_REQUEST, "필수 헤더가 없습니다", Map.of("header", e.getHeaderName()));
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    ResponseEntity<ErrorResponse> unreadable(HttpMessageNotReadableException e) {
        return respond(ErrorCode.INVALID_REQUEST, "요청 본문을 읽을 수 없습니다", Map.of());
    }
}
