// 방 이벤트 클라이언트 테스트 (node --test, Node 22+)
import { test } from 'node:test';
import assert from 'node:assert/strict';
import { connectRoomEvents, parseSseBlock } from '../../main/resources/static/js/room-events.js';

const enc = new TextEncoder();

// SSE 응답. open이면 abort 전까지 안 닫힘
function sse(chunks, { open = false, signal } = {}) {
  const body = new ReadableStream({
    start(c) {
      chunks.forEach((x) => c.enqueue(enc.encode(x)));
      if (!open) c.close();
      else signal?.addEventListener('abort', () => c.error(new DOMException('aborted', 'AbortError')));
    },
  });
  return new Response(body, { status: 200 });
}

const json = (body, status = 200) => new Response(JSON.stringify(body), { status });

const event = (rev) => `id:${rev}\nevent:participant.joined\ndata:${JSON.stringify({ roomRev: rev, type: 'participant.joined', payload: {} })}\n\n`;

const ready = (rev) => `event:ready\ndata:{"roomRev":${rev}}\n\n`;

// cond가 참이 될 때까지 최대 1초
async function until(cond) {
  for (let i = 0; i < 100 && !cond(); i += 1) await new Promise((r) => setTimeout(r, 10));
  assert.ok(cond(), '시간 초과');
}

test('SSE 블록 파싱 (주석 무시)', () => {
  assert.deepEqual(parseSseBlock('id:2\nevent:state.changed\ndata:{"roomRev":2}'),
    { id: '2', event: 'state.changed', data: '{"roomRev":2}' });
  assert.equal(parseSseBlock(':ping').data, '');
});
