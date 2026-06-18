import { adminGuard } from "../middleware/admin.js";
import { getStore } from "../repository/index.js";
import { toNdjson } from "../util/ndjson.js";
import { putToS3 } from "../util/s3.js";
import { hookRequeue } from "../webhook.js";
import { M } from "../metrics.js";
export default async function route(app) {
    app.get("/admin/dlq/receipt", async (request) => { adminGuard(request); const store = await getStore(); const query = request.query || {}; return store.dlqSearch("receipt", { afterId: Number(query.afterId || 0), limit: Math.min(1000, Number(query.limit || 100)), apiKey: query.apiKey ? String(query.apiKey) : undefined, reason: query.reason ? String(query.reason) : undefined, statusMin: query.statusMin != null ? Number(query.statusMin) : undefined, statusMax: query.statusMax != null ? Number(query.statusMax) : undefined, q: query.q ? String(query.q) : undefined }); });
    app.delete("/admin/dlq/receipt/:id", async (request, reply) => { adminGuard(request); await (await getStore()).dlqDelete(Number(request.params.id)); return reply.status(204).send(); });
    app.post("/admin/dlq/receipt/requeue/:id", async (request, reply) => { adminGuard(request); const store = await getStore(); const item = await store.dlqGet(Number(request.params.id)); if (!item)
        return reply.status(404).send({ error: "not_found" }); const { verifyReceiptViaCore } = await import("../adapter/http/commerce.js"); const output = await verifyReceiptViaCore(item.payload); if (output.status >= 400) {
        await store.dlqUpdateAttempt(item.id, item.attempts + 1, Date.now() + 60_000);
        await M.inc("dlqRequeue", { mode: "manual", result: "fail" });
        await hookRequeue({ mode: "manual", id: item.id, reason: item.reason, apiKey: item.apiKey || null, attempts: item.attempts + 1, status: output.status });
        return reply.send({ requeued: false, status: output.status, body: output.body });
    } await store.dlqDelete(item.id); await M.inc("dlqRequeue", { mode: "manual", result: "success" }); await hookRequeue({ mode: "manual", id: item.id, reason: item.reason, apiKey: item.apiKey || null, attempts: item.attempts, status: output.status }); return reply.send({ requeued: true, status: output.status, body: output.body }); });
    app.get("/admin/dlq/receipt/export", async (request, reply) => { adminGuard(request); const format = String(request.query?.format || "ndjson"); const limit = Math.min(100000, Number(request.query?.limit || 10000)); const rows = await (await getStore()).dlqSearch("receipt", { limit }); if (format === "json")
        return reply.type("application/json").send(rows); reply.header("Content-Disposition", "attachment; filename=dlq-receipt.ndjson"); return reply.type("application/x-ndjson").send(toNdjson(rows)); });
    app.post("/admin/dlq/receipt/export/s3", async (request) => { adminGuard(request); const limit = Math.min(100000, Number(request.body?.limit || 10000)); const rows = await (await getStore()).dlqSearch("receipt", { limit }); const timestamp = new Date().toISOString().replace(/[:.]/g, "-"); const key = `dlq/receipt-${timestamp}.ndjson`; return { ok: true, s3: await putToS3(key, toNdjson(rows), "application/x-ndjson") }; });
}
