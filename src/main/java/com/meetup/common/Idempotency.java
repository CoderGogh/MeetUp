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

    // 요청 내용 지문 (같은 키를 다른 요청에 쓰는지 판별)
    public String fingerprint(Object... parts) {
        try {
            return Hashes.sha256(json.writeValueAsString(parts));
        } catch (JsonProcessingException e) {
            throw new IllegalStateException(e);
        }
    }

    // 이미 처리된 키면 저장된 응답, 처리 중이면 409, 다른 요청이면 422
    private ResponseEntity<Object> replay(String redisKey, String fingerprint) {
        String stored = redis.opsForValue().get(redisKey);
        if (stored == null) {
            throw new ApiException(ErrorCode.IDEMPOTENCY_IN_PROGRESS);
        }
        IdempotencyRecord record = read(stored);
        if (!record.fingerprint().equals(fingerprint)) {
            throw new ApiException(ErrorCode.IDEMPOTENCY_KEY_REUSED);
        }
        if (!record.done()) {
            throw new ApiException(ErrorCode.IDEMPOTENCY_IN_PROGRESS);
        }
        return ResponseEntity.status(record.status()).header(REPLAYED, "true").body(record.body());
    }

    // 선점한 요청만 실행. 실패하면 키를 지워 재시도가 다시 실행되게 함
    private ResponseEntity<Object> run(String redisKey, String fingerprint, Supplier<ResponseEntity<?>> action) {
        try {
            ResponseEntity<?> response = action.get();
            var done = new IdempotencyRecord(fingerprint, response.getStatusCode().value(),
                    json.valueToTree(response.getBody()));
            keepTtlSet(redisKey, write(done));
            return ResponseEntity.status(response.getStatusCode()).headers(response.getHeaders())
                    .body(response.getBody());
        } catch (RuntimeException e) {
            redis.delete(redisKey);
            throw e;
        }
    }

    /** scope 안에서 key를 선점한 요청만 action 실행. 보존 기간 ttl은 방 TTL 이하로 넘김 */
    public ResponseEntity<Object> execute(String scope, String key, String fingerprint, Duration ttl,
            Supplier<ResponseEntity<?>> action) {
        if (key == null || !KEY.matcher(key).matches()) {
            throw new ApiException(ErrorCode.INVALID_REQUEST, "요청 키 형식이 올바르지 않습니다",
                    Map.of("header", HEADER, "pattern", KEY.pattern()));
        }
        String redisKey = "idem:{" + scope + "}:" + key;
        Boolean first = redis.opsForValue().setIfAbsent(redisKey, write(IdempotencyRecord.pending(fingerprint)), ttl);
        return Boolean.TRUE.equals(first) ? run(redisKey, fingerprint, action) : replay(redisKey, fingerprint);
    }
}
