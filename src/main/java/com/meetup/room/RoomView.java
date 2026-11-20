package com.meetup.room;

import com.meetup.place.Theme;
import java.time.Instant;
import java.util.List;

/** 방 조회 응답 */
public record RoomView(String code, Theme theme, int capacity, RoomState state, long roomRev,
        Instant expiresAt, String inviteUrl, String me, List<Member> participants) {

    /** 참여자 공개 정보 */
    public record Member(String id, String nickname, boolean host, boolean originSubmitted) {

        static Member of(Participant p) {
            return new Member(p.id(), p.nickname(), p.host(), p.originSubmitted());
        }
    }
}
