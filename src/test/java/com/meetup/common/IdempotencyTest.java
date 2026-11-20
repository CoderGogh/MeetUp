package com.meetup.common;

import java.time.Duration;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

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

    @Test
    void replaysFirstResultWithoutRerun() {
        call("key-00001", "a");
        ResponseEntity<Object> again = call("key-00001", "a");
        assertThat(runs.get()).isEqualTo(1);
        assertThat(again.getStatusCode().value()).isEqualTo(201);
        assertThat(again.getHeaders().getFirst(Idempotency.REPLAYED)).isEqualTo("true");
        assertThat(again.getBody().toString()).contains("\"run\":1");
    }

    @Test
    void rejectsKeyReusedForDifferentRequest() {
        call("key-00002", "a");
        assertThatThrownBy(() -> call("key-00002", "b")).isInstanceOf(ApiException.class)
                .extracting("code").isEqualTo(ErrorCode.IDEMPOTENCY_KEY_REUSED);
    }

    @Test
    void pendingKeyIsInProgress() {
        idempotency.execute(scope, "key-00003", "a", ttl, () -> {
            assertThatThrownBy(() -> call("key-00003", "a")).isInstanceOf(ApiException.class)
                    .extracting("code").isEqualTo(ErrorCode.IDEMPOTENCY_IN_PROGRESS);
            return ResponseEntity.ok().build();
        });
    }
}
