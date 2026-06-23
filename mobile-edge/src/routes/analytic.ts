
import { FastifyInstance } from "fastify";
import { verifyAnalyticSignature } from "../middleware/analytic.js";
import { registry } from "../contract/registry.js";
import { parseNdjson } from "../util/ndjson.js";
import { getStore } from "../repository/index.js";

export default async function route(app: FastifyInstance){
  app.post("/analytic/batch", {
    schema: registry.get("analytic.batch.json", registry.latest("analytic.batch.json")!),
    config: { rateLimit: { max: 60, timeWindow: "1 minute" } }
  }, async (req: any, res) => {
    const raw = Buffer.from(JSON.stringify(req.body || {}));
    verifyAnalyticSignature(req, raw);
    const events = (req.body?.events || []) as any[];
    const store = await getStore();
    for (const ev of events){ if (typeof ev?.type === "string") await store.metricIncr("analytic_"+ev.type, 1); }
    return res.status(202).send({ accepted: true, mode: "json", events: events.length });
  });
  app.post("/analytic/batch-ndjson", {
    config: { rawBody: true, rateLimit: { max: 60, timeWindow: "1 minute" } }
  }, async (req: any, res) => {
    const raw: Buffer = req.rawBody || Buffer.from("");
    verifyAnalyticSignature(req, raw);
    const items = parseNdjson(raw);
    const store = await getStore();
    for (const ev of items){ if (typeof ev?.type === "string") await store.metricIncr("analytic_"+ev.type, 1); }
    return res.status(202).send({ accepted: true, mode: "ndjson", events: items.length });
  });
}
