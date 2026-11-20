package com.meetup.room;

import com.meetup.common.ApiException;
import com.meetup.common.ErrorCode;
import java.text.Normalizer;
import java.util.Locale;
import java.util.Map;

/** 별명 정리·검증. 중복 판단은 대소문자 무시 */
public final class Nicknames {

    static final int MAX_LENGTH = 12;

    private Nicknames() {
    }

    public static String clean(String raw) {
        String s = raw == null ? "" : Normalizer.normalize(raw, Normalizer.Form.NFC).strip();
        int length = s.codePointCount(0, s.length());
        if (length == 0 || length > MAX_LENGTH || s.codePoints().anyMatch(Nicknames::invisible)) {
            throw new ApiException(ErrorCode.INVALID_REQUEST, "별명은 1~12자, 보이는 글자만 쓸 수 있습니다",
                    Map.of("fields", Map.of("nickname", "1~12자")));
        }
        return s;
    }

    // 중복 판단 키
    public static String key(String cleaned) {
        return cleaned.toLowerCase(Locale.ROOT);
    }

    private static boolean invisible(int cp) {
        int type = Character.getType(cp);
        return type == Character.CONTROL || type == Character.FORMAT;
    }
}
