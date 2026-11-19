-- 기준 역 (환승역은 1행)
CREATE TABLE IF NOT EXISTS station (
    id          SERIAL PRIMARY KEY,
    name        VARCHAR(50)  NOT NULL UNIQUE,
    lines       VARCHAR(120) NOT NULL,
    geom        geometry(Point, 4326) NOT NULL,
    created_at  TIMESTAMPTZ  NOT NULL DEFAULT now()
);

-- 최근접 역 탐색
CREATE INDEX IF NOT EXISTS idx_station_geom ON station USING GIST (geom);
