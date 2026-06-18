export class RuntimeStore {
    idempotency = new Map();
    device = new Map();
    metric = new Map();
    apiKey = new Map();
    dlq = [];
    webhook = new Map();
    delivery = [];
    dlqSequence = 0;
    deliverySequence = 0;
    async init() { }
    async idemGet(key) { const item = this.idempotency.get(key); if (!item || item.expiresAt < Date.now()) {
        this.idempotency.delete(key);
        return undefined;
    } return { status: item.status, body: item.body }; }
    async idemPut(key, status, body, ttlMs = 3_600_000) { this.idempotency.set(key, { status, body, expiresAt: Date.now() + ttlMs }); }
    async deviceRegister(deviceId, token, platform) { this.device.set(deviceId, { token, platform, updatedAt: Date.now() }); }
    async deviceCount() { return this.device.size; }
    async metricIncr(name, delta = 1) { const bucket = Math.floor(Date.now() / 60_000); const series = this.metric.get(name) || new Map(); series.set(bucket, (series.get(bucket) || 0) + delta); this.metric.set(name, series); }
    async metricRecent(name, limit = 60) { return [...(this.metric.get(name) || new Map()).entries()].sort(([left], [right]) => right - left).slice(0, limit).map(([bucket, count]) => ({ bucket, count })); }
    async apikeyGet(key) { return this.apiKey.get(key); }
    async apikeyUpsert(key, name, quota_rpm, enabled, requeue_rpm, requeue_daily) { this.apiKey.set(key, { key, name, quota_rpm, enabled, requeue_rpm, requeue_daily }); }
    async apikeyList() { return [...this.apiKey.values()]; }
    async apikeyConsume(key, amount, windowMs, limit) { return this.consume(`key:${key}`, amount, windowMs, limit); }
    async budgetConsume(name, windowMs, limit) { return this.consume(name, 1, windowMs, limit); }
    async dlqPush(type, idem, payload, status, reason, apiKey) { this.dlq.push({ id: ++this.dlqSequence, type, reason, idem, apiKey, payload, status, attempts: 0, ts: Date.now(), next: 0 }); }
    async dlqList(type, afterId = 0, limit = 100) { return this.dlq.filter((item) => item.type === type && item.id > afterId).slice(0, limit); }
    async dlqSearch(type, search) { return this.dlq.filter((item) => item.type === type && (!search.afterId || item.id > search.afterId) && (!search.apiKey || item.apiKey === search.apiKey) && (!search.reason || item.reason === search.reason) && (search.statusMin === undefined || item.status >= search.statusMin) && (search.statusMax === undefined || item.status <= search.statusMax) && (!search.q || JSON.stringify(item.payload).includes(search.q))).slice(0, Math.min(1000, search.limit || 100)); }
    async dlqGet(id) { return this.dlq.find((item) => item.id === id); }
    async dlqDelete(id) { const index = this.dlq.findIndex((item) => item.id === id); if (index >= 0)
        this.dlq.splice(index, 1); }
    async dlqDue(type, now, limit) { return this.dlq.filter((item) => item.type === type && (item.next === 0 || item.next <= now)).slice(0, limit); }
    async dlqUpdateAttempt(id, attempts, next) { const item = await this.dlqGet(id); if (item) {
        item.attempts = attempts;
        item.next = next;
    } }
    async dlqPurgeExpired(beforeTs) { const initial = this.dlq.length; for (let index = this.dlq.length - 1; index >= 0; index--)
        if (this.dlq[index].ts < beforeTs)
            this.dlq.splice(index, 1); return initial - this.dlq.length; }
    async whUpsert(subscription) { this.webhook.set(subscription.id, subscription); }
    async whList() { return [...this.webhook.values()]; }
    async whFindByEvent(event) { return [...this.webhook.values()].filter((item) => item.enabled && item.events.includes(event)); }
    async whEnqueue(subId, event, payload) { const id = ++this.deliverySequence; this.delivery.push({ id, subId, event, payload, status: "pending", attempts: 0, next: 0, ts: Date.now() }); return id; }
    async whDue(now, limit) { return this.delivery.filter((item) => item.status !== "sent" && (item.next === 0 || item.next <= now)).slice(0, limit); }
    async whUpdateAttempt(id, attempts, next, lastStatus, durationMs) { const item = await this.whGet(id); if (item) {
        item.attempts = attempts;
        item.next = next;
        item.lastStatus = lastStatus;
        item.durationMs = durationMs;
        item.status = "failed";
    } }
    async whMarkSent(id, lastStatus, durationMs) { const item = await this.whGet(id); if (item) {
        item.status = "sent";
        item.lastStatus = lastStatus;
        item.durationMs = durationMs;
    } }
    async whGet(id) { return this.delivery.find((item) => item.id === id); }
    async whSearch(event, status, sinceTs, limit = 200) { return this.delivery.filter((item) => (!event || item.event === event) && (!status || item.status === status) && (!sinceTs || item.ts >= sinceTs)).slice(-Math.min(1000, limit)).reverse(); }
    async consume(name, amount, windowMs, limit) { const bucket = Math.floor(Date.now() / windowMs); const series = this.metric.get(name) || new Map(); const count = (series.get(bucket) || 0) + amount; series.set(bucket, count); this.metric.set(name, series); return count <= limit; }
}
