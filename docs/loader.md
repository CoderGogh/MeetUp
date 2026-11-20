# 이동시간 행렬 적재

경로 API는 이 적재에서만 부른다. 서비스 런타임은 적재된 행렬만 읽는다.

## 실행

```bash
docker compose up -d postgis
./gradlew bootRun --args='--spring.profiles.active=loader'
```

- 웹 서버 없이 뜨고, 적재가 끝나면 종료된다
- 하루 한 번 돌리면 된다. 멈춘 곳부터 이어서 한다
