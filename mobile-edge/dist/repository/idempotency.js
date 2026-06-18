const map = new Map();
const TTL_MS = 60 * 60 * 1000;
export function put(key, status, body) {
    const exp = Date.now() + TTL_MS;
    map.set(key, { status, body, exp });
}
export function get(key) {
    const v = map.get(key);
    if (!v)
        return;
    if (v.exp < Date.now()) {
        map.delete(key);
        return;
    }
    return v;
}
export function sweep() {
    const now = Date.now();
    for (const [k, v] of map)
        if (v.exp < now)
            map.delete(k);
}
setInterval(sweep, 60_000).unref();
