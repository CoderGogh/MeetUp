-- 역 × 후보지 대중교통 소요시간 (사전 적재)
CREATE TABLE IF NOT EXISTS travel_time (
    station_id    INT         NOT NULL REFERENCES station (id),
    candidate_id  INT         NOT NULL REFERENCES candidate (id),
    minutes       SMALLINT    NOT NULL CHECK (minutes >= 0),
    transfers     SMALLINT    NOT NULL DEFAULT 0 CHECK (transfers >= 0),
    source        VARCHAR(10) NOT NULL CHECK (source IN ('ODSAY', 'ESTIMATE')),
    updated_at    TIMESTAMPTZ NOT NULL DEFAULT now(),
    PRIMARY KEY (station_id, candidate_id)
);

-- 오래된 행 우선 재조회
CREATE INDEX IF NOT EXISTS idx_travel_time_updated ON travel_time (updated_at);
