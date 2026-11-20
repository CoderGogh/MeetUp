package com.meetup.room;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class ParticipantApiTest {

    @Autowired
    MockMvc mvc;
    @Autowired
    RoomService rooms;
    @Autowired
    ObjectMapper json;
    @Autowired
    StringRedisTemplate redis;
}
