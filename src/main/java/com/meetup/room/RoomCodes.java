package com.meetup.room;

import java.security.SecureRandom;
import java.util.regex.Pattern;

/** 방 코드. 헷갈리는 글자(0·1·i·l·o) 뺀 31자 중 12자, 약 59비트 (NFR-08) */
public final class RoomCodes {

    static final String ALPHABET = "23456789abcdefghjkmnpqrstuvwxyz";
    static final int LENGTH = 12;
    private static final Pattern VALID = Pattern.compile("[" + ALPHABET + "]{" + LENGTH + "}");
    private static final SecureRandom RANDOM = new SecureRandom();

    private RoomCodes() {
    }

    public static String generate() {
        StringBuilder code = new StringBuilder(LENGTH);
        for (int i = 0; i < LENGTH; i++) {
            code.append(ALPHABET.charAt(RANDOM.nextInt(ALPHABET.length())));
        }
        return code.toString();
    }

    // 형식이 틀린 코드는 Redis 조회 전에 거름
    public static boolean isValid(String code) {
        return code != null && VALID.matcher(code).matches();
    }
}
