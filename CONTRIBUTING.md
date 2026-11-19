# 형상 관리 규칙

## 브랜치

`main`은 보호 브랜치다. 직접 push하지 않고 PR로만 병합한다.

| 접두어 | 용도 | 예 |
|---|---|---|
| `feat/` | 기능 추가·수정 | `feat/room-state-machine` |
| `fix/` | 버그 수정 | `fix/vote-recount` |
| `hotfix/` | 데모 운영 중 긴급 수정 | `hotfix/sse-timeout` |
| `ci/` | CI 설정 | `ci/github-actions` |
| `build/` | 의존성·컨테이너 | `build/postgis-redis-compose` |
| `docs/` | 문서 | `docs/service-concept` |
| `refactor/`, `test/`, `chore/` | 그 외 | `test/vote-concurrency` |
| `release/` | 릴리즈 준비 | `release/v0.1.0` |

- 이름은 소문자와 하이픈만 쓴다.
- 한 브랜치에는 기능 하나만 담는다.

## 커밋

- 한 줄, `type: 설명` 형식
  - type: `feat` `fix` `build` `chore` `ci` `docs` `style` `refactor` `test` `release`
  - 예: `feat: 방 상태 전이 허용 목록`
- 잘게 나눈다. 커밋 하나의 변경(추가·삭제 줄)은 1,200자 이하
  - 새 파일도 클래스·메서드·테스트·문서 절 단위로 나눠 쌓는다
- 검사가 실패하면 커밋하지 않는다 (`./gradlew check && git commit ...`)
