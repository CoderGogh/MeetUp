# 실시간 이벤트

## 구독

`GET /rooms/{code}/events` · 헤더 `X-Participant-Token` → `text/event-stream`

- 참여자만 구독한다. 토큰이 없거나 틀리면 401, 없는 방 404, 만료된 방 410. 오류 본문은 다른 API와 같은 JSON
- 연결 직후 `ready`로 현재 `roomRev`를 준다. 들고 있는 값보다 크면 `GET /rooms/{code}`로 다시 맞춘다
- 20초마다 `:ping` 주석을 보낸다. 프록시 유휴 끊김을 막고, 끊긴 연결을 이때 정리한다
- 서버는 30분마다 연결을 닫는다. 클라이언트가 다시 붙는다

## 이벤트 형식

```
id:2
event:participant.joined
data:{"code":"…","roomRev":2,"type":"participant.joined","payload":{"nickname":"준호","count":2}}
```

- `id`는 `roomRev`다. 방 안에서 이벤트마다 1씩 오른다
- 클라이언트는 들고 있는 값 이하의 이벤트를 버린다. 순서가 뒤집혀 와도 화면이 과거로 돌아가지 않는다 (RFP 8.6.3)
- `roomRev`를 올리는 코드는 반드시 이벤트를 같이 낸다

## 이벤트 목록

| type | 언제 | payload |
|---|---|---|
| `participant.joined` | 입장 | `nickname`, `count` |
| `state.changed` | 상태 전이 | `from`, `to` |
| `origin.submitted` | 출발지 입력 (4주차) | `nickname`, 입력 완료 수 |
| `participant.left` | 퇴장 (4주차) | `nickname`, `count` |
| `candidates.ready` · `vote.updated` · `draw.result` · `room.confirmed` | 5주차 | |

전달 지연은 `DeliveryLatencyTest`로 잰다. 구독자 5명에게 입장 이벤트가 1초 안에 가야 통과 (NFR-06)
