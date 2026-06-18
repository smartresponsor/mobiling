
type Entry = { status: number; body: any; exp: number };
const map = new Map<string, Entry>();
const TTL_MS = 60 * 60 * 1000;
export function put(key: string, status: number, body: any) {
  const exp = Date.now() + TTL_MS;
  map.set(key, { status, body, exp });
}
export function get(key: string): Entry | undefined {
  const v = map.get(key);
  if (!v) return;
  if (v.exp < Date.now()) { map.delete(key); return; }
  return v;
}
export function sweep() {
  const now = Date.now();
  for (const [k, v] of map) if (v.exp < now) map.delete(k);
}
setInterval(sweep, 60_000).unref();
