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
