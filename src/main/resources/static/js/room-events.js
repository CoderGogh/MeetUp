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
