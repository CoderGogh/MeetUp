package com.meetup.loader;

/** 외부 API 호출 예산. 실제 호출 직전에 1건 차감하고, 남은 게 없으면 한도 초과 */
public interface CallBudget {

    // 테스트·추정 모드용
    CallBudget UNLIMITED = () -> {
    };

    void acquire();
}
