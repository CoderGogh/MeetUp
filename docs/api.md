# API 공통 규칙

## 오류 응답

모든 오류는 같은 형식이다.

```json
{ "code": "INVALID_TRANSITION", "message": "지금 상태에서는 할 수 없는 요청입니다", "details": { "currentState": "VOTING", "roomRev": 7 } }
```

| code | HTTP | 언제 |
|---|---|---|
| `INVALID_REQUEST` | 400 | 형식·검증 실패, 필수 헤더 없음 (`details.fields`·`details.header`) |
| `UNAUTHORIZED` | 401 | 참여자 토큰 없음·불일치 |
| `FORBIDDEN` | 403 | 방장만 할 수 있는 요청 |
| `ROOM_NOT_FOUND` · `NOT_FOUND` | 404 | 없는 방 · 없는 경로 |
| `ROOM_EXPIRED` | 410 | 만료된 방 |
| `NICKNAME_TAKEN` · `ROOM_FULL` | 409 | 별명 중복 · 정원 초과 |
| `INVALID_TRANSITION` · `PRECONDITION_FAILED` | 409 | 금지 전이 · 전이 조건 미충족. 현재 상태를 같이 줌 |
| `IDEMPOTENCY_IN_PROGRESS` | 409 | 같은 요청 키를 처리 중 |
| `IDEMPOTENCY_KEY_REUSED` | 422 | 다른 요청에 쓴 요청 키 |
| `INTERNAL` | 500 | 서버 오류. 원인은 응답에 싣지 않고 로그에만 |

## 멱등성 키

상태를 바꾸는 요청(방 생성·입장·상태 전이, 이후 투표·확정·추첨)은 `Idempotency-Key` 헤더가 필수다.

- 형식: 영문·숫자·`-`·`_` 8~64자. 클라이언트가 요청마다 새로 만든다 (UUID 권장)
- 처음 도착한 요청만 실행하고, 같은 키 재전송은 처음 응답을 그대로 돌려준다 (`Idempotent-Replayed: true`)
- 같은 키를 다른 내용의 요청에 쓰면 422, 처음 요청을 아직 처리 중이면 409
- 처리 중 오류가 나면 키를 풀어서 같은 키로 다시 보낼 수 있다
- 보존 기간은 방 TTL을 넘지 않는다. 결과를 저장할 때도 처음 잡은 만료 시각을 유지한다
