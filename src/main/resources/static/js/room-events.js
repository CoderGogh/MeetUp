// 방 이벤트 구독. SSE가 연달아 실패하면 폴링으로 강등 (RFP 6.8)
// EventSource는 헤더를 못 실어 fetch 스트림으로 SSE를 읽음

// SSE 블록 하나 → {id, event, data}. 주석(:)은 무시
export function parseSseBlock(block) {
  const out = { id: null, event: 'message', data: '' };
  const data = [];
  for (const line of block.split(/\r?\n/)) {
    if (!line || line.startsWith(':')) continue;
    const i = line.indexOf(':');
    const field = i < 0 ? line : line.slice(0, i);
    const value = i < 0 ? '' : line.slice(i + 1).replace(/^ /, '');
    if (field === 'data') data.push(value);
    else if (field === 'id') out.id = value;
    else if (field === 'event') out.event = value;
  }
  out.data = data.join('\n');
  return out;
}

const FATAL = new Set([401, 403, 404, 410]);

const wait = (ms) => new Promise((resolve) => setTimeout(resolve, ms));

export function connectRoomEvents({
  code,
  token,
  lastRev = 0,
  onEvent,
  onResync = () => {},
  onMode = () => {},
  onError = () => {},
  maxFailures = 3,
  retryMs = 1000,
  readyMs = 5000,
  pollMs = 3000,
  fetchImpl = (...args) => fetch(...args),
}) {
  let rev = lastRev;
  let mode = 'sse';
  let failures = 0;
  let closed = false;
  let controller = null;
  let timer = null;

  // 가진 rev 이하는 버림 (순서가 뒤집혀도 화면이 과거로 안 감)
  const accept = (event) => {
    if (event.roomRev <= rev) return;
    rev = event.roomRev;
    onEvent(event);
  };

  // 보관 범위 밖. 호출한 쪽이 전체를 다시 조회
  const resync = (roomRev) => {
    rev = Math.max(rev, roomRev);
    onResync(roomRev);
  };

  const stop = (status) => {
    closed = true;
    onError(status);
  };

  const headers = (type) => ({ Accept: type, 'X-Participant-Token': token });

  // ready를 받으면 정상 연결로 보고 실패 횟수 초기화
  const dispatch = ({ event, data }, ready) => {
    if (!data) return;
    const body = JSON.parse(data);
    if (event === 'ready') {
      ready();
      if (body.roomRev > rev) resync(body.roomRev);
    } else if (event === 'resync') {
      resync(body.roomRev);
    } else {
      accept(body);
    }
  };

  // 연결 하나. 끊기면 반환, 응답 오류는 throw
  async function stream() {
    controller = new AbortController();
    const h = headers('text/event-stream');
    if (rev > 0) h['Last-Event-ID'] = String(rev);
    const res = await fetchImpl(`/rooms/${code}/events`, { headers: h, signal: controller.signal });
    if (FATAL.has(res.status)) return stop(res.status);
    if (!res.ok || !res.body) throw new Error(`sse ${res.status}`);
    // 버퍼링 프록시는 연결만 되고 이벤트가 안 옴 → ready 시간 초과로 끊음
    const guard = setTimeout(() => controller.abort(), readyMs);
    const ready = () => {
      clearTimeout(guard);
      failures = 0;
    };
    const reader = res.body.getReader();
    const decoder = new TextDecoder();
    let buf = '';
    try {
      for (;;) {
        const { value, done } = await reader.read();
        if (done) return;
        buf += decoder.decode(value, { stream: true }).replace(/\r\n/g, '\n');
        for (let i = buf.indexOf('\n\n'); i >= 0; i = buf.indexOf('\n\n')) {
          dispatch(parseSseBlock(buf.slice(0, i)), ready);
          buf = buf.slice(i + 2);
        }
      }
    } finally {
      clearTimeout(guard);
    }
  }

  async function pollOnce() {
    const res = await fetchImpl(`/rooms/${code}/events?since=${rev}`, { headers: headers('application/json') });
    if (FATAL.has(res.status)) return stop(res.status);
    if (!res.ok) return;
    const page = await res.json();
    if (page.resync) resync(page.roomRev);
    else page.events.forEach(accept);
  }

  function startPolling() {
    mode = 'poll';
    onMode('poll');
    const tick = async () => {
      if (closed) return;
      try {
        await pollOnce();
      } catch {
        // 다음 주기에 다시
      }
      if (!closed) timer = setTimeout(tick, pollMs);
    };
    tick();
  }

  // 끊길 때마다 다시 붙고, maxFailures번 연달아 실패하면 폴링
  async function run() {
    while (!closed) {
      try {
        await stream();
      } catch {
        // 재시도로
      }
      if (closed) return;
      failures += 1;
      if (failures >= maxFailures) return startPolling();
      await wait(retryMs * failures);
    }
  }

  run();
  return {
    get mode() {
      return mode;
    },
    get rev() {
      return rev;
    },
    close() {
      closed = true;
      if (controller) controller.abort();
      clearTimeout(timer);
    },
  };
}
