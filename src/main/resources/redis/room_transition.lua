-- 상태 전이 CAS. 단계 변경과 roomRev 증가를 한 번에 (RFP 8.4.3)
-- KEYS: 방   ARGV: 기대 단계, 다음 단계
-- 반환: {1, 다음 단계, rev} 성공 | {0, 현재 단계, rev} 다른 요청이 먼저 바꿈 | {-1, '', 0} 방 없음
if redis.call('EXISTS', KEYS[1]) == 0 then
  return {-1, '', 0}
end
local current = redis.call('HGET', KEYS[1], 'state')
if current ~= ARGV[1] then
  return {0, current, tonumber(redis.call('HGET', KEYS[1], 'rev'))}
end
redis.call('HSET', KEYS[1], 'state', ARGV[2])
local rev = redis.call('HINCRBY', KEYS[1], 'rev', 1)
return {1, ARGV[2], rev}
