-- 방 생성 (방장 참여자 포함). 코드가 이미 있으면 0
-- KEYS: 방, 참여자, 별명, 토큰
-- ARGV: 키 TTL(초), 테마, 정원, 방장 토큰 해시, 생성 시각, 만료 시각,
--       참여자 ID, 참여자 JSON, 별명 키, 참여자 토큰 해시
if redis.call('EXISTS', KEYS[1]) == 1 then
  return 0
end
redis.call('HSET', KEYS[1], 'theme', ARGV[2], 'capacity', ARGV[3], 'state', 'COLLECTING', 'rev', '1',
  'hostTokenHash', ARGV[4], 'createdAt', ARGV[5], 'expiresAt', ARGV[6])
redis.call('HSET', KEYS[2], ARGV[7], ARGV[8])
redis.call('HSET', KEYS[3], ARGV[9], ARGV[7])
redis.call('HSET', KEYS[4], ARGV[10], ARGV[7])
for i = 1, 4 do
  redis.call('EXPIRE', KEYS[i], ARGV[1])
end
return 1
