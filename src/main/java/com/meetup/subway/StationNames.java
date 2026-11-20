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

    // 원본 노선명 → 그래프 노선명. 그래프 밖 노선은 null
    public static String line(String route) {
        String r = route.trim();
        if (r.matches("[1-8]")) {
            return r + "호선";
        }
        if (r.matches("[1-8]호선") || r.equals("9호선") || r.equals("9호선(연장)")) {
            return r.substring(0, 3);
        }
        return switch (r) {
            case "경부선", "경원선", "경인선" -> "1호선";
            case "경의중앙선", "중앙선" -> "경의중앙선";
            case "공항철도", "공항철도1호선" -> "공항철도";
            case "분당선", "수인분당선" -> "수인분당선";
            default -> r.startsWith("신분당선") ? "신분당선" : null;
        };
    }
}
