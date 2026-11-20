package com.meetup.loader;

/** 경로 조회 결과 */
public record Route(int minutes, int transfers, Source source) {

    /** 소요시간 출처 */
    public enum Source {
        ODSAY,
        ESTIMATE
    }
}
