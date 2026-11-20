package com.meetup.loader;

/** 외부 API 호출 한도 초과. 남은 조합을 실패로 돌리지 않고 실행을 멈춤 */
public class QuotaExceededException extends RouteException {

    public QuotaExceededException(String message) {
        super(message);
    }
}
