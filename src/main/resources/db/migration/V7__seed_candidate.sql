-- 후보지 30곳 (선정 근거 docs/candidates.md, 좌표는 기준역과 동일)
INSERT INTO candidate (name, station_id, area_type, region, theme_tags, underground, geom)
SELECT v.name, s.id, v.area, 'GANGNAM', v.tags::VARCHAR(10)[], v.underground, s.geom
FROM (VALUES
    ('강남',       'LARGE',  '{CASUAL,DATE,INDOOR}',  true),
    ('고속터미널', 'LARGE',  '{MEETING,DATE,INDOOR}', true),
    ('사당',       'MEDIUM', '{CASUAL,MEETING}',      false),
    ('선릉',       'MEDIUM', '{MEETING,TALK}',        false),
    ('삼성',       'LARGE',  '{DATE,INDOOR}',         true),
    ('양재',       'MEDIUM', '{MEETING,TALK}',        false),
    ('노량진',     'SMALL',  '{MEETING,TALK}',        false)
) AS v (name, area, tags, underground)
JOIN station s ON s.name = v.name
ON CONFLICT (name) DO NOTHING;

INSERT INTO candidate (name, station_id, area_type, region, theme_tags, underground, geom)
SELECT v.name, s.id, v.area, 'EAST', v.tags::VARCHAR(10)[], v.underground, s.geom
FROM (VALUES
    ('잠실',     'LARGE',  '{CASUAL,DATE,INDOOR}', true),
    ('건대입구', 'LARGE',  '{CASUAL,DATE}',        false),
    ('왕십리',   'MEDIUM', '{MEETING,INDOOR}',     true),
    ('성수',     'MEDIUM', '{DATE,TALK}',          false),
    ('천호',     'MEDIUM', '{CASUAL,INDOOR}',      true),
    ('청량리',   'MEDIUM', '{MEETING,CASUAL}',     false),
    ('군자',     'SMALL',  '{MEETING,TALK}',       false)
) AS v (name, area, tags, underground)
JOIN station s ON s.name = v.name
ON CONFLICT (name) DO NOTHING;
