-- 방 이벤트 보관. roomRev를 점수로 넣고 최근 N개만 남김, 방 키와 같은 시각에 만료
-- KEYS: 이벤트, 방   ARGV: roomRev, 이벤트 JSON, 보관 개수
-- 반환: 1 보관 | 0 방 없음
local ttl = redis.call('PTTL', KEYS[2])
if ttl <= 0 then
  return 0
end
redis.call('ZADD', KEYS[1], ARGV[1], ARGV[2])
redis.call('ZREMRANGEBYRANK', KEYS[1], 0, -tonumber(ARGV[3]) - 1)
redis.call('PEXPIRE', KEYS[1], ttl)
return 1
