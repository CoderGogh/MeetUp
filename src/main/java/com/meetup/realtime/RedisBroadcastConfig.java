package com.meetup.realtime;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;

/** Redis 전파일 때만 채널 구독 */
@Configuration
@ConditionalOnProperty(name = "meetup.realtime.broadcast", havingValue = "redis", matchIfMissing = true)
class RedisBroadcastConfig {

    @Bean
    RedisMessageListenerContainer roomEventListener(RedisConnectionFactory redis, ObjectMapper json,
            RoomStreams streams) {
        RedisMessageListenerContainer container = new RedisMessageListenerContainer();
        container.setConnectionFactory(redis);
        container.addMessageListener(new RedisEventSubscriber(json, streams),
                new ChannelTopic(RedisBroadcaster.CHANNEL));
        return container;
    }
}
