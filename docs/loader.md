# 이동시간 행렬 적재

기본은 지하철 그래프 계산(`LOADER_MODE=graph`)이다. 외부 호출 없이 역 × 후보지 전부를 채운다. ODsay(`LOADER_MODE=odsay`)는 무료 한도가 월 30건이라 표본 검증에만 쓴다. 서비스 런타임은 적재된 행렬만 읽는다.

## 실행

```bash
docker compose up -d postgis
set -a && . ./.env && set +a   # ODSAY_API_KEY 등
./gradlew bootRun --args='--spring.profiles.active=loader'
```

- 웹 서버 없이 뜨고, 적재가 끝나면 종료된다
- 그래프 방식은 몇 초 안에 끝난다. 역이나 후보지가 바뀌면 다시 돌린다

## 설정

| 환경변수 | 기본값 | 내용 |
|---|---|---|
| `LOADER_MODE` | graph | `graph` 그래프 계산, `odsay` ODsay 호출 |
| `SEOUL_OPENAPI_KEY` | 없음 | 있으면 기준역을 역사마스터에서 먼저 받음 |
| `ODSAY_API_KEY` | 없음 | odsay 방식에서 없으면 추정 모드 |
| `STATIONS_CSV` | 없음 | 기준역 CSV 경로. 주면 행렬 적재 전에 먼저 넣음 |
| `LOADER_DAILY_LIMIT` | 0 | 1회 실행당 ODsay 호출 상한. 0이면 호출 안 함. 무료 한도가 월 30건이라 검증할 때만 명시 |

그 밖에 `--meetup.loader.max-attempts=3`(실패 재시도), `batch-size=20`, `delay=200ms`(호출 간격), `refresh-older-than=180d`(오래된 행 재조회)를 인자로 바꿀 수 있다.

## ODsay 방식 동작

1. `station × candidate` 중 `load_progress`에 없는 조합만 `PENDING`으로 추가
2. `PENDING` 먼저, 그다음 시도 횟수가 남은 `FAILED` 순으로 20개씩 조회
3. 성공분은 `travel_time` upsert와 `DONE` 표시를 한 트랜잭션으로 저장
4. 실패는 `FAILED`·시도 횟수·오류 코드만 남긴다. 인증키는 메시지에 넣지 않는다
5. ODsay 호출이 한도에 닿거나 ODsay가 `429`(한도 초과)를 주면 멈춘다. 한도 초과로 못 부른 조합은 실패로 세지 않는다

같은 역은 0분, 700m 이내는 도보 시간으로 바로 채운다 (ODsay가 700m 이내를 지원하지 않음). 이 조합은 호출 수에 들어가지 않는다.

중간에 죽으면 저장 전 배치(최대 20건)만 다시 부른다.

## 추정 모드

키가 없으면 직선거리로 채운다. `source = 'ESTIMATE'`로 남기므로 나중에 키를 넣고 다시 돌리면 갈아끼울 수 있다.

- 소요 = 직선거리 × 1.3(우회) ÷ 30km/h + 6분(승강장·대기)
- 환승은 추정하지 않는다 (0)
- 개발·테스트용. 데모 데이터는 ODsay로 채운다

추정 행을 ODsay로 바꾸려면 해당 조합을 다시 `PENDING`으로 돌린다.

```sql
UPDATE load_progress p SET status = 'PENDING', attempts = 0
FROM travel_time t
WHERE t.station_id = p.station_id AND t.candidate_id = p.candidate_id AND t.source = 'ESTIMATE';
```

## 기준역 (서울시 역사마스터)

`SEOUL_OPENAPI_KEY`가 있으면 실행할 때 [역사마스터 정보](https://data.seoul.go.kr/dataList/OA-21232/S/1/datasetView.do) Open API(784행)를 받아 기준역부터 넣는다.

- 그래프(`docs/subway-data.md`)에 있는 역·노선만 넣는다. 지금 301역
- 역명은 괄호를 빼고 개명 역은 새 이름으로 맞춘다 (`당고개`→`불암산`, `뚝섬유원지`→`자양`, `이수`→`총신대입구`)
- 노선은 그래프 노선명으로 적는다 (`경부선`·`경원선` → `1호선`, `분당선` → `수인분당선`)
- 키는 URL 경로에 들어간다. 오류 메시지에는 HTTP 상태와 `INFO-100` 같은 결과 코드만 남긴다

## 기준역 CSV

한 행이 역 × 노선이다. 같은 역명은 노선을 합쳐 1행으로 넣고, 이미 있는 역은 노선·좌표를 덮어쓴다.

```csv
name,line,lat,lng
강남,2호선,37.4979,127.0276
강남,신분당선,37.4979,127.0276
```

- 헤더는 영문(`name` `line` `lat` `lng`)이나 공공데이터 한글 헤더(`역사명` `호선` `위도` `경도`) 둘 다 읽는다
- UTF-8, MS949 모두 읽는다. `01호선`은 `1호선`으로 바꾼다
- 서울 열린데이터광장 "서울시 역사마스터 정보" CSV를 그대로 넣을 수 있다. MVP 범위(약 300역)에 맞게 노선을 거른 뒤 넣는다
- 역명은 후보 기준역 시드(`V6`)와 같아야 같은 역으로 합쳐진다
