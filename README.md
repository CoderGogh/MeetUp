# 어디가?

누가 얼마나 더 멀리 오는지 숫자로 보여주고, 그 합의를 링크 하나로 끝내는 모임 장소 결정 서비스.

- 후보지마다 참여자 전원의 대중교통 이동시간을 한 화면에
- 가장 공평한 곳(최장 이동시간 최소)과 가장 덜 움직이는 곳(총 이동시간 최소)을 함께 제시
- 로그인 없이 링크로 입장, 실시간 동시 투표로 확정

## 문서

- [서비스 컨셉](docs/concept.md)
- [기초 기획안](docs/plan.md)
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
