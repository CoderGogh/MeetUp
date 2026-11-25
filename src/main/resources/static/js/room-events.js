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
