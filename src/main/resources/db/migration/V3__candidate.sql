-- 만남 후보지 (수작업 큐레이션)
CREATE TABLE IF NOT EXISTS candidate (
    id           SERIAL PRIMARY KEY,
    name         VARCHAR(50) NOT NULL UNIQUE,
    station_id   INT         NOT NULL REFERENCES station (id),
    area_type    VARCHAR(10) NOT NULL CHECK (area_type IN ('LARGE', 'MEDIUM', 'SMALL')),
    region       VARCHAR(10) NOT NULL CHECK (region IN ('GANGNAM', 'GANGBUK', 'WEST', 'EAST')),
    theme_tags   VARCHAR(10)[] NOT NULL DEFAULT '{}',
    underground  BOOLEAN     NOT NULL DEFAULT false,
    geom         geometry(Point, 4326) NOT NULL
);

CREATE INDEX IF NOT EXISTS idx_candidate_station ON candidate (station_id);
