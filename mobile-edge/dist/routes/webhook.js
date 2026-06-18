import { adminGuard } from "../middleware/admin.js";
import { getStore } from "../repository/index.js";
export default async function route(app) {
    app.get("/admin/webhook/subscription", async (request) => { adminGuard(request); return (await getStore()).whList(); });
    app.post("/admin/webhook/subscription", async (request, reply) => { adminGuard(request); const { id, url, secret, enabled = true, events = [], retry_max = 12, backoff_base_ms = 1000, backoff_cap_ms = 600000 } = request.body || {}; if (!id || !url || !secret || !Array.isArray(events) || events.length === 0)
        return reply.status(400).send({ error: "bad_request" }); await (await getStore()).whUpsert({ id: String(id), url: String(url), secret: String(secret), enabled: Boolean(enabled), events: events.map(String), retry_max: Number(retry_max), backoff_base_ms: Number(backoff_base_ms), backoff_cap_ms: Number(backoff_cap_ms) }); return { ok: true }; });
    app.get("/admin/webhook/delivery", async (request) => { adminGuard(request); const query = request.query || {}; return (await getStore()).whSearch(query.event, query.status, query.sinceTs ? Number(query.sinceTs) : undefined, Math.min(1000, Number(query.limit || 200))); });
    app.post("/admin/webhook/delivery/replay/:id", async (request, reply) => { adminGuard(request); const store = await getStore(); const delivery = await store.whGet(Number(request.params.id)); if (!delivery)
        return reply.status(404).send({ error: "not_found" }); await store.whUpdateAttempt(delivery.id, delivery.attempts, 0, delivery.lastStatus ?? null, delivery.durationMs ?? null); return { ok: true, id: delivery.id }; });
}
