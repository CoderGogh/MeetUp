package com.meetup.loader;

/** 경로 조회 실패 (메시지에 인증키 미포함) */
public class RouteException extends RuntimeException {

    public RouteException(String message) {
        super(message);
    }
}
