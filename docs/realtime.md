# 실시간 이벤트

## 구독

`GET /rooms/{code}/events` · 헤더 `X-Participant-Token` → `text/event-stream`

- 참여자만 구독한다. 토큰이 없거나 틀리면 401, 없는 방 404, 만료된 방 410. 오류 본문은 다른 API와 같은 JSON
- 연결 직후 `ready`로 현재 `roomRev`를 준다. 들고 있는 값보다 크면 `GET /rooms/{code}`로 다시 맞춘다
- 20초마다 `:ping` 주석을 보낸다. 프록시 유휴 끊김을 막고, 끊긴 연결을 이때 정리한다
- 서버는 30분마다 연결을 닫는다. 클라이언트가 다시 붙는다
