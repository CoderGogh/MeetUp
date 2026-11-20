package com.meetup.room;

import java.util.List;

/** 방 Redis 키. 해시 태그 {코드}로 한 방의 키는 같은 슬롯 */
final class RoomKeys {

    private RoomKeys() {
    }

    static String room(String code) {
        return "room:{" + code + "}";
    }

    static String participants(String code) {
        return room(code) + ":participants";
    }

    static String nicknames(String code) {
        return room(code) + ":nicknames";
    }

    static String tokens(String code) {
        return room(code) + ":tokens";
    }

    static List<String> all(String code) {
        return List.of(room(code), participants(code), nicknames(code), tokens(code));
    }
}
