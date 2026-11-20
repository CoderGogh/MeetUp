package com.meetup.common;

import java.time.Duration;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")
class IdempotencyTest {

    @Autowired
    Idempotency idempotency;
    @Autowired
    StringRedisTemplate redis;

    final String scope = "test-" + UUID.randomUUID();
    final AtomicInteger runs = new AtomicInteger();
    final Duration ttl = Duration.ofMinutes(5);

    ResponseEntity<Object> call(String key, String fingerprint) {
        return idempotency.execute(scope, key, fingerprint, ttl, () -> {
            runs.incrementAndGet();
            return ResponseEntity.status(201).body(Map.of("run", runs.get()));
        });
    }
}
