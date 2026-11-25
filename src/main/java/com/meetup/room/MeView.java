package com.meetup.room;

/** 내 상태 (재접속 복구, FR-21). 투표는 5주차에 추가 */
public record MeView(String participantId, String nickname, boolean host, boolean originSubmitted,
        RoomState state, long roomRev) {

    static MeView of(Participant p, RoomState state, long roomRev) {
        return new MeView(p.id(), p.nickname(), p.host(), p.originSubmitted(), state, roomRev);
    }
}
