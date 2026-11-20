package com.meetup.room;

import java.security.SecureRandom;
import java.util.Base64;

/** 참여자·방장 토큰 (256비트). 서버는 해시만 저장 */
public final class Tokens {

    private static final SecureRandom RANDOM = new SecureRandom();

    private Tokens() {
    }

    public static String generate() {
        return random(32);
    }

    // 참여자 ID (공개값, 8바이트)
    public static String id() {
        return random(8);
    }

    private static String random(int bytes) {
        byte[] b = new byte[bytes];
        RANDOM.nextBytes(b);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(b);
    }
}
