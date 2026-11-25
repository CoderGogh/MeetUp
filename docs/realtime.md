# 실시간 이벤트

## 구독

`GET /rooms/{code}/events` · 헤더 `X-Participant-Token` → `text/event-stream`

- 참여자만 구독한다. 토큰이 없거나 틀리면 401, 없는 방 404, 만료된 방 410. 오류 본문은 다른 API와 같은 JSON
- 연결 직후 `ready`로 현재 `roomRev`를 준다. 들고 있는 값보다 크면 `GET /rooms/{code}`로 다시 맞춘다
- 20초마다 `:ping` 주석을 보낸다. 프록시 유휴 끊김을 막고, 끊긴 연결을 이때 정리한다
- 서버는 30분마다 연결을 닫는다. 클라이언트가 다시 붙는다

## 재접속

끊겼다 다시 붙을 때 마지막으로 받은 이벤트 ID를 `Last-Event-ID` 헤더로 보낸다. 브라우저 `EventSource`는 알아서 보낸다

- 그 뒤 이벤트를 순서대로 다시 보내고 `ready`를 보낸다
- 놓친 구간이 보관 범위를 넘으면 이벤트 대신 `resync`(`roomRev`)를 보낸다. 클라이언트는 `GET /rooms/{code}`·`GET /rooms/{code}/participants/me`로 전체를 다시 맞춘다
- 숫자가 아닌 값은 400 (`details.header`)
- 재전송과 실시간 전달이 겹친 이벤트는 한 번만 보낸다. 그래도 같은 ID가 두 번 오면 클라이언트가 버린다

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

## 이벤트 보관

- Redis `room:{코드}:events` 정렬 집합. 점수가 `roomRev`, 값이 이벤트 JSON
- 방별 최근 200개(`meetup.realtime.retained-events`)만 남긴다. 방 키와 같은 시각에 만료
- 보관과 전파는 같은 리스너에서 보관 → 전파 순. 보관이 실패해도 전파는 하고, 그 구간은 재접속 때 `resync`로 메운다
- 끊김 검증은 `ReconnectTest`. 실제 연결을 끊고 그 사이 입장·전이를 낸 뒤 재접속해 다 받는지, 최종 `roomRev`·단계·인원이 같은지 본다 (NFR-07)

## 다중 인스턴스

SSE 연결은 붙은 인스턴스 메모리에만 있다. 인스턴스가 둘이면 A에서 난 이벤트를 B에 붙은 참여자가 못 받는다. 그래서 이벤트를 Redis 채널 `meetup:room-events`로 발행하고, 모든 인스턴스가 구독해 자기 연결에 나눠 준다

- `meetup.realtime.broadcast` (`REALTIME_BROADCAST`): `redis`(기본) · `local`(단일 인스턴스, 채널 구독 안 함)
- 자기 인스턴스 구독자도 채널을 거쳐 받는다. 경로가 하나라 같은 이벤트가 두 번 가지 않는다
- 이벤트 보관은 Redis라 다른 인스턴스로 재접속해도 끊긴 구간을 받는다
- 발행이 실패하면 이 인스턴스 구독자에게라도 보내고 경고 로그를 남긴다. 나머지는 재접속 때 메운다
