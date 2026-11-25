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

## 방

| 메서드 | 경로 | 헤더 | 응답 |
|---|---|---|---|
| POST | `/rooms` | `Idempotency-Key` | 201 `{code, inviteUrl, state, roomRev, expiresAt, participantId, participantToken, hostToken}` |
| GET | `/rooms/{code}` | `X-Participant-Token` | 200 `{code, theme, capacity, state, roomRev, expiresAt, inviteUrl, me, participants[]}` |

생성 요청: `{"theme": "CASUAL", "capacity": 5, "nickname": "민지"}`. 테마는 `MEETING` `TALK` `CASUAL` `DATE` `INDOOR`, 정원 2~10, 별명 1~12자.

- 방장은 만든 순간 첫 참여자로 들어간다. 참여자 토큰과 방장 토큰을 따로 받는다
- 방 코드는 헷갈리는 글자를 뺀 31자 중 12자 (약 59비트). 순차 증가하지 않는다

**만료와 저장**

- 방은 생성 후 6시간(`ROOM_TTL`)에 만료. 만료 뒤 1시간은 조회하면 410 `ROOM_EXPIRED`, 그 뒤 키가 지워지면 404
- Redis 키 `room:{코드}`, `:participants`, `:nicknames`, `:tokens` 네 개가 같은 TTL로 함께 사라진다
- 토큰은 SHA-256 해시만 저장한다. 원문은 생성·입장 응답에만 실린다
- 단, 재전송에 같은 응답을 돌려주려고 그 응답 본문(토큰 포함)을 멱등성 기록에 보존 기간 동안 둔다. 보존 기간은 방 TTL 이하

## 입장

| 메서드 | 경로 | 헤더 | 응답 |
|---|---|---|---|
| POST | `/rooms/{code}/participants` | `Idempotency-Key` | 201 `{participantId, participantToken, roomRev}` |

요청: `{"nickname": "준호"}`. 링크(방 코드)만 있으면 누구나 들어올 수 있다 (FR-02).

- 정원·별명 중복·단계 검사와 등록을 Lua 스크립트 하나로 처리한다. 동시에 몰려도 정원을 넘지 않고, 같은 별명은 한 명만 들어간다
- 별명 중복은 대소문자 무시. 같은 사람이 두 기기로 들어오는 것도 별명 중복으로 막는다
- `COLLECTING`·`REVIEWING`에서만 입장. 그 외 단계는 409 `INVALID_TRANSITION` + `currentState`
- 입장하면 `roomRev`가 1 오르고 `participant.joined` 이벤트를 낸다 ([실시간 이벤트](realtime.md))
- 이후 요청은 `X-Participant-Token`으로 본인을 밝힌다. 별명은 로그에 남기지 않는다

## 내 상태

| 메서드 | 경로 | 헤더 | 응답 |
|---|---|---|---|
| GET | `/rooms/{code}/participants/me` | `X-Participant-Token` | 200 `{participantId, nickname, host, originSubmitted, state, roomRev}` |

새로고침·재접속 때 현재 단계와 내 입력값을 되살린다 (FR-21). 투표 항목은 5주차에 붙는다. 토큰이 없거나 틀리면 401, 만료된 방 410
