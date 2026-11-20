package com.meetup.common;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Map;
import java.util.function.Supplier;
import java.util.regex.Pattern;
import org.springframework.data.redis.connection.RedisStringCommands.SetOption;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.types.Expiration;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

/** 멱등성 키 선점, 재전송엔 처음 결과 반환 (RFP 8.5.1) */
@Component
public class Idempotency {

    public static final String HEADER = "Idempotency-Key";
    public static final String REPLAYED = "Idempotent-Replayed";
    private static final Pattern KEY = Pattern.compile("[A-Za-z0-9_-]{8,64}");

    private final StringRedisTemplate redis;
    private final ObjectMapper json;

    public Idempotency(StringRedisTemplate redis, ObjectMapper json) {
        this.redis = redis;
        this.json = json;
    }

    private String write(IdempotencyRecord record) {
        try {
            return json.writeValueAsString(record);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException(e);
        }
    }

    private IdempotencyRecord read(String stored) {
        try {
            return json.readValue(stored, IdempotencyRecord.class);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException(e);
        }
    }

    // 선점 때 잡은 만료 시각 유지 (방 TTL을 넘지 않게)
    private void keepTtlSet(String key, String value) {
        redis.execute((RedisCallback<Boolean>) c -> c.stringCommands().set(
                key.getBytes(StandardCharsets.UTF_8), value.getBytes(StandardCharsets.UTF_8),
                Expiration.keepTtl(), SetOption.upsert()));
    }
}
