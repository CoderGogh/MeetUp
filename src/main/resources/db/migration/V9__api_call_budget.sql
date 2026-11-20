-- 외부 API 월별 호출 수 (무료 한도 보호, 월은 KST 기준 YYYY-MM)
CREATE TABLE IF NOT EXISTS api_call_budget (
    provider  VARCHAR(20) NOT NULL,
    month     CHAR(7)     NOT NULL,
    calls     INT         NOT NULL DEFAULT 0 CHECK (calls >= 0),
    PRIMARY KEY (provider, month)
);
