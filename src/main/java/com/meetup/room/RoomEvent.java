package com.meetup.room;

import java.util.Map;

/** 방 이벤트 (SSE 전파는 3주차). roomRev는 단조 증가 (RFP 8.6.3) */
public record RoomEvent(String code, long roomRev, String type, Map<String, Object> payload) {
}
