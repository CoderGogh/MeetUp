-- 입장. 정원·별명 중복·단계 검사와 등록을 한 번에
-- KEYS: 방, 참여자, 별명, 토큰
-- ARGV: 현재 시각(ms), 참여자 ID, 참여자 JSON, 별명 키, 토큰 해시
-- 반환: {결과, 값, 입장 당시 단계}  결과 = OK(rev) | GONE | EXPIRED | STATE(단계) | FULL(정원) | TAKEN
if redis.call('EXISTS', KEYS[1]) == 0 then
  return {'GONE', '', ''}
end
local room = redis.call('HMGET', KEYS[1], 'state', 'capacity', 'expiresAt')
if tonumber(room[3]) <= tonumber(ARGV[1]) then
  return {'EXPIRED', room[3], room[1]}
end
if room[1] ~= 'COLLECTING' and room[1] ~= 'REVIEWING' then
  return {'STATE', room[1], room[1]}
end
if redis.call('HLEN', KEYS[2]) >= tonumber(room[2]) then
  return {'FULL', room[2], room[1]}
end
if redis.call('HSETNX', KEYS[3], ARGV[4], ARGV[2]) == 0 then
  return {'TAKEN', '', room[1]}
end
redis.call('HSET', KEYS[2], ARGV[2], ARGV[3])
redis.call('HSET', KEYS[4], ARGV[5], ARGV[2])
local rev = redis.call('HINCRBY', KEYS[1], 'rev', 1)
return {'OK', tostring(rev), room[1]}
