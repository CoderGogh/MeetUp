-- 지하철 그래프 최단 경로 출처 추가
ALTER TABLE travel_time DROP CONSTRAINT IF EXISTS travel_time_source_check;
ALTER TABLE travel_time ADD CONSTRAINT travel_time_source_check
    CHECK (source IN ('ODSAY', 'ESTIMATE', 'GRAPH'));
