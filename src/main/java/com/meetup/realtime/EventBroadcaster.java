package com.meetup.realtime;

import com.meetup.room.RoomEvent;

/** 방 이벤트를 구독자가 붙은 인스턴스로 보냄 */
interface EventBroadcaster {

    void publish(RoomEvent e);
}
