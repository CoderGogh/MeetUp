-- 행렬 적재 진행 상태 (중단·재개)
CREATE TABLE IF NOT EXISTS load_progress (
    station_id    INT          NOT NULL REFERENCES station (id),
    candidate_id  INT          NOT NULL REFERENCES candidate (id),
    status        VARCHAR(10)  NOT NULL DEFAULT 'PENDING'
                  CHECK (status IN ('PENDING', 'DONE', 'FAILED')),
    attempts      SMALLINT     NOT NULL DEFAULT 0,
    last_error    VARCHAR(200),
    updated_at    TIMESTAMPTZ  NOT NULL DEFAULT now(),
    PRIMARY KEY (station_id, candidate_id)
);

CREATE INDEX IF NOT EXISTS idx_load_progress_status ON load_progress (status);
