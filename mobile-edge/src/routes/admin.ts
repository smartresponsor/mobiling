
import { FastifyInstance } from "fastify";
import { adminGuard } from "../middleware/admin.js";
import { getStore } from "../repository/index.js";
import { toNdjson } from "../util/ndjson.js";
import { putToS3 } from "../util/s3.js";
import { hookRequeue } from "../webhook.js";
import { M } from "../metrics.js";

export default async function route(app: FastifyInstance){
  app.get("/admin/dlq/receipt", async (req:any) => {
    adminGuard(req);
    const s = await getStore();
    const q = req.query || {};
    const rows = await s.dlqSearch("receipt", {
      afterId: Number(q.afterId || 0),
      limit: Math.min(1000, Number(q.limit || 100)),
      apiKey: q.apiKey ? String(q.apiKey) : undefined,
      reason: q.reason ? String(q.reason) : undefined,
      statusMin: q.statusMin != null ? Number(q.statusMin) : undefined,
      statusMax: q.statusMax != null ? Number(q.statusMax) : undefined,
      q: q.q ? String(q.q) : undefined
    });
    return rows;
  });
  app.delete("/admin/dlq/receipt/:id", async (req:any, res) => {
    adminGuard(req);
    const s = await getStore();
    await s.dlqDelete(Number(req.params.id));
    return res.status(204).send();
  });
  app.post("/admin/dlq/receipt/requeue/:id", async (req:any, res) => {
    adminGuard(req);
    const s = await getStore();
    const item = await s.dlqGet(Number(req.params.id));
    if (!item) return res.status(404).send({ error: "not_found" });
    const { verifyReceiptViaCore } = await import("../adapter/http/commerce.js");
    const out = await verifyReceiptViaCore(item.payload);
    if (out.status >= 400){
      await s.dlqUpdateAttempt(item.id, item.attempts + 1, Date.now() + 60_000);
      await M.inc("dlqRequeue", { mode:"manual", result:"fail" });
      await hookRequeue({ mode:"manual", id:item.id, reason:item.reason, apiKey:item.apiKey||null, attempts:item.attempts+1, status: out.status });
      return res.status(200).send({ requeued: false, status: out.status, body: out.body });
    } else {
      await s.dlqDelete(item.id);
      await M.inc("dlqRequeue", { mode:"manual", result:"success" });
      await hookRequeue({ mode:"manual", id:item.id, reason:item.reason, apiKey:item.apiKey||null, attempts:item.attempts, status: out.status });
      return res.status(200).send({ requeued: true, status: out.status, body: out.body });
    }
  });
  app.get("/admin/dlq/receipt/export", async (req:any, res) => {
    adminGuard(req);
    const format = (req.query?.format || "ndjson").toString();
    const limit = Math.min(100000, Number(req.query?.limit || 10000));
    const s = await getStore();
    const rows = await s.dlqSearch("receipt", { limit });
    if (format === "json"){
      res.type("application/json").send(rows);
    } else {
      const buf = toNdjson(rows);
      res.header("Content-Disposition","attachment; filename="dlq-receipt.ndjson"");
      res.type("application/x-ndjson").send(buf);
    }
  });
  app.post("/admin/dlq/receipt/export/s3", async (req:any) => {
    adminGuard(req);
    const limit = Math.min(100000, Number(req.body?.limit || 10000));
    const s = await getStore();
    const rows = await s.dlqSearch("receipt", { limit });
    const buf = toNdjson(rows);
    const ts = new Date().toISOString().replace(/[:.]/g,"-");
    const key = `dlq/receipt-${ts}.ndjson`;
    const out = await putToS3(key, buf, "application/x-ndjson");
    return { ok: true, s3: out };
  });
}
