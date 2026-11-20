package com.meetup.subway;

import java.util.HashMap;
import java.util.Map;

/** 환승 보행 시간 (서울교통공사 환승역 데이터) */
public final class TransferTimes {

    private final Map<String, Double> minutes;

    private TransferTimes(Map<String, Double> minutes) {
        this.minutes = minutes;
    }

    public static TransferTimes load() {
        Map<String, Double> m = new HashMap<>();
        for (String[] r : SubwaySources.rows("seoul_metro_transfer.csv")) {
            String from = StationNames.line(r[1]);
            String to = StationNames.line(r[3]);
            if (from != null && to != null) {
                m.put(key(StationNames.normalize(r[2]), from, to), SubwaySources.minutes(r[5]));
            }
        }
        return new TransferTimes(m);
    }

    // 데이터 없으면 null
    public Double walk(String station, String from, String to) {
        if (from.replace("급행", "").equals(to.replace("급행", ""))) {
            return 0.0;
        }
        Double m = minutes.get(key(station, from, to));
        return m != null ? m : minutes.get(key(station, to, from));
    }

    private static String key(String station, String from, String to) {
        return station + ":" + from + ":" + to;
    }
}
