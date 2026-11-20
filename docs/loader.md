# 이동시간 행렬 적재

경로 API는 이 적재에서만 부른다. 서비스 런타임은 적재된 행렬만 읽는다.

## 실행

```bash
docker compose up -d postgis
./gradlew bootRun --args='--spring.profiles.active=loader'
```

- 웹 서버 없이 뜨고, 적재가 끝나면 종료된다
- 하루 한 번 돌리면 된다. 멈춘 곳부터 이어서 한다

## 설정

| 환경변수 | 기본값 | 내용 |
|---|---|---|
| `ODSAY_API_KEY` | 없음 | 없으면 추정 모드 |
| `STATIONS_CSV` | 없음 | 기준역 CSV 경로. 주면 행렬 적재 전에 먼저 넣음 |
| `LOADER_DAILY_LIMIT` | 1000 | 1회 실행당 ODsay 호출 상한 (무료 쿼터 일 1,000건) |

그 밖에 `--meetup.loader.max-attempts=3`(실패 재시도), `batch-size=20`, `delay=200ms`(호출 간격), `refresh-older-than=180d`(오래된 행 재조회)를 인자로 바꿀 수 있다.
