package com.meetup.subway;

import java.util.Map;

/** 원본마다 다른 역명을 한 이름으로 */
public final class StationNames {

    // 개명·표기 차이 (원본 → 기준)
    private static final Map<String, String> ALIASES = Map.of(
            "당고개", "불암산",
            "신내역", "신내",
            "이수", "총신대입구",
            "뚝섬유원지", "자양");

    private StationNames() {
    }

    public static String normalize(String name) {
        String plain = name.replaceAll("\\(.*?\\)", "").trim();
        return ALIASES.getOrDefault(plain, plain);
    }
}
