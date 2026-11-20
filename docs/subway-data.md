# 지하철 그래프 원본 데이터

ODsay 무료 한도(월 30건)로는 행렬 9,000조합을 채울 수 없다. 공공데이터로 노선 그래프를 만들어 최단 시간을 직접 계산하고, ODsay는 표본 검증에만 쓴다.

## 출처

| 파일 (`src/main/resources/subway/`) | 원본 | 기준일 | 이용 조건 |
|---|---|---|---|
| `seoul_metro_interstation.csv` | 서울 열린데이터광장 [OA-12034](https://data.seoul.go.kr/dataList/OA-12034/S/1/datasetView.do) 서울교통공사 역간거리 및 소요시간 | 2024-08-10 | 출처표시 |
| `seoul_metro_transfer.csv` | 공공데이터포털 [15044419](https://www.data.go.kr/data/15044419/fileData.do) 서울교통공사 환승역거리 소요시간 정보 | 2026-08-31 | 공공데이터포털 이용허락 |
| `extra_lines.csv` | 직접 정리 (아래) | | |

원본은 MS949 CSV다. UTF-8로 바꾸고 줄바꿈만 정리했다. 값은 손대지 않았다.
