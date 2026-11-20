# 방 상태 전이

```
COLLECTING ──후보지 산출(방장)──▶ REVIEWING ──투표 시작(방장)──▶ VOTING ──확정(방장)──▶ CONFIRMED
     ▲                              │                              │
     └──────참여자 입장(시스템)─────┘                              │
     └──────────────────────────재입력(방장)───────────────────────┘
모든 단계 ──만료 시각 경과──▶ EXPIRED
```

| 현재 | 이벤트 (`trigger`) | 조건 | 다음 | 요청 |
|---|---|---|---|---|
| COLLECTING | `CALCULATE_CANDIDATES` | 참여자 2명 이상 · 전원 출발지 입력 | REVIEWING | 방장 |
| REVIEWING | `START_VOTING` | | VOTING | 방장 |
| REVIEWING | `PARTICIPANT_CHANGED` | | COLLECTING | 시스템 (입장 시) |
| VOTING | `RESET_INPUT` | | COLLECTING | 방장 |
| VOTING | `CONFIRM` | 득표 1위 확정 | CONFIRMED | 방장 |
| 전체 | 만료 시각 경과 | | EXPIRED | 시스템 |

표에 없는 쌍은 전부 금지다. 건너뛰기, 그 밖의 역방향, CONFIRMED·EXPIRED 이후 모든 전이, 팀원이 요청하는 모든 전이가 여기 해당한다.

## 구현

- 단계를 바꾸는 코드는 `RoomStateMachine.fire` 한 곳뿐이다. 입장 시 되돌림도 이 진입점을 지난다
- 순서: 권한 → 허용 목록 → 조건 → CAS. 금지 전이는 409 `INVALID_TRANSITION`, 조건 미충족은 409 `PRECONDITION_FAILED`. 둘 다 `currentState`·`roomRev`를 같이 준다 (클라이언트가 화면을 맞춤)
- CAS는 Lua 스크립트(`room_transition.lua`) 하나로 "기대 단계면 바꾸고 `roomRev` +1"을 원자 처리한다. 동시에 10건이 와도 1건만 반영된다
- 만료는 저장된 단계가 아니라 만료 시각으로 판단한다. 지나면 어떤 전이든 `currentState: EXPIRED`로 거부
- 성공하면 `state.changed` 이벤트(`from`·`to`·`roomRev`)를 내고 `room.transition code=… trigger=… from=… to=… rev=…` 로그를 남긴다

## API

`POST /rooms/{code}/transitions` · 헤더 `Idempotency-Key`, `X-Host-Token` · 본문 `{"trigger": "START_VOTING"}` → 200 `{from, state, roomRev}`

방장 토큰이 아니면 참여자 토큰으로 팀원인지 확인해 403, 둘 다 없거나 틀리면 401. 후보지 산출·확정 API(4~5주차)도 같은 진입점을 쓴다.
