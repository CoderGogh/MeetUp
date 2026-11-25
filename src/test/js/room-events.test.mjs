// 방 이벤트 클라이언트 테스트 (node --test, Node 22+)
import { test } from 'node:test';
import assert from 'node:assert/strict';
import { connectRoomEvents, parseSseBlock } from '../../main/resources/static/js/room-events.js';

test('SSE 블록 파싱 (주석 무시)', () => {
  assert.deepEqual(parseSseBlock('id:2\nevent:state.changed\ndata:{"roomRev":2}'),
    { id: '2', event: 'state.changed', data: '{"roomRev":2}' });
  assert.equal(parseSseBlock(':ping').data, '');
});
