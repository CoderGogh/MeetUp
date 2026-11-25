# 어디가?

만남 장소 정하기 힘들 때! 쉽고 편하게 정하자~!

모임 당사자 위치 거리를 고려하고, 어디서 무엇을 할지, 어떤 방식으로 놀지! 한번에 정하기 !

- 후보지마다 참여자 전원의 대중교통 이동시간을 한 화면에
- 가장 공평한 곳(최장 이동시간 최소)과 가장 덜 움직이는 곳(총 이동시간 최소)을 함께 제시
- 로그인 없이 링크로 입장, 실시간 동시 투표로 확정

## 문서

- [서비스 컨셉](docs/concept.md)
- [기초 기획안](docs/plan.md)
- [후보지 선정](docs/candidates.md)
- [이동시간 행렬 적재](docs/loader.md)
- [API 공통 규칙](docs/api.md)
- [방 상태 전이](docs/state-machine.md)
- [실시간 이벤트](docs/realtime.md)
- [형상 관리 규칙](CONTRIBUTING.md)
## 실행

```bash
cp .env.example .env          # 키 입력 (없어도 기동은 됨)
docker compose up -d --build  # PostGIS · Redis · 앱
curl localhost:8080/actuator/health
```

- 두 번째 인스턴스까지 띄우기: `docker compose --profile scale up -d`
- 종료: `docker compose down` (데이터 볼륨 유지, `-v` 붙이면 삭제)
- 로컬 개발: PostGIS·Redis만 compose로 띄우고 `./gradlew bootRun`
- 이동시간 행렬 적재: `./gradlew bootRun --args='--spring.profiles.active=loader'` ([방법](docs/loader.md))
