package com.meetup.room;

import java.util.List;

/** 방 Redis 키. 해시 태그 {코드}로 한 방의 키는 같은 슬롯 */
public final class RoomKeys {

    private RoomKeys() {
    }

    public static String room(String code) {
        return "room:{" + code + "}";
    }

    public static String participants(String code) {
        return room(code) + ":participants";
    }

    public static String nicknames(String code) {
        return room(code) + ":nicknames";
    }

    public static String tokens(String code) {
        return room(code) + ":tokens";
    }

    public static List<String> all(String code) {
        return List.of(room(code), participants(code), nicknames(code), tokens(code));
    }
}
