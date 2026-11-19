#!/bin/bash
# 스테이징 변경의 .env 비밀값 검사 (KEY·PASSWORD·SECRET·TOKEN, URL 인코딩형 포함). 발견 시 종료 코드 1
# pre-commit 훅으로 쓰려면: ln -s ../../scripts/check_secrets.sh .git/hooks/pre-commit
ROOT="$(git rev-parse --show-toplevel)"
ENV_FILE="$ROOT/.env"
[ -f "$ENV_FILE" ] || exit 0
