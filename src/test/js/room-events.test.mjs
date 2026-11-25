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

test('가진 rev 이하 이벤트는 버림', async () => {
  const got = [];
  const fetchImpl = async (url, { signal }) => sse([ready(1), event(3), event(2), event(4)], { open: true, signal });
  const c = connectRoomEvents({ code: 'C', token: 't', lastRev: 1, onEvent: (e) => got.push(e.roomRev), fetchImpl });
  await until(() => got.length === 2);
  c.close();
  assert.deepEqual(got, [3, 4]);
  assert.equal(c.rev, 4);
});

test('재접속 때 Last-Event-ID로 가진 rev 전송', async () => {
  let sent;
  const fetchImpl = async (url, { headers, signal }) => {
    sent = headers;
    return sse([ready(5)], { open: true, signal });
  };
  const c = connectRoomEvents({ code: 'C', token: 't', lastRev: 5, onEvent: () => {}, fetchImpl });
  await until(() => sent);
  c.close();
  assert.equal(sent['Last-Event-ID'], '5');
  assert.equal(sent['X-Participant-Token'], 't');
});

test('SSE가 연달아 실패하면 폴링으로 강등', async () => {
  const calls = [];
  const got = [];
  let mode;
  const fetchImpl = async (url) => {
    calls.push(url);
    if (!url.includes('since=')) return new Response('', { status: 502 });
    return json({ roomRev: 2, resync: false, events: [{ roomRev: 2, type: 'participant.joined', payload: {} }] });
  };
  const c = connectRoomEvents({ code: 'C', token: 't', lastRev: 1, onEvent: (e) => got.push(e.roomRev),
    onMode: (m) => { mode = m; }, retryMs: 1, pollMs: 10, fetchImpl });
  await until(() => got.length === 1);
  c.close();
  assert.equal(mode, 'poll');
  assert.equal(calls.filter((u) => !u.includes('since=')).length, 3);
  assert.ok(calls.includes('/rooms/C/events?since=1'));
});
