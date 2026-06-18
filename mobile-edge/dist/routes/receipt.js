import { requireJwt } from "../middleware/jwt.js";
import { registry } from "../contract/registry.js";
import { getStore } from "../repository/index.js";
import { verifyReceiptViaCore } from "../adapter/http/commerce.js";
import { M } from "../metrics.js";
export default async function route(app) {
    app.post("/mobile/receipt/verify", {
        schema: registry.get("receipt.verify", registry.latest("receipt.verify"))
    }, async (req, res) => {
        await requireJwt(req);
        const idem = (req.headers["idempotency-key"] || "");
        const apiKey = (req.headers["x-api-key"] || req.headers["x-sr-key"] || "") || null;
        const store = await getStore();
        if (idem) {
            const cached = await store.idemGet(idem);
            if (cached) {
                await M.inc("receipt", { outcome: "cached" });
                return res.status(cached.status).send(cached.body);
            }
        }
        const payload = req.body || {};
        try {
            const out = await verifyReceiptViaCore(payload);
            if (out.status >= 400) {
                await store.dlqPush("receipt", idem || "no-idem", payload, out.status, out.status >= 500 ? "server_error" : "client_error", apiKey);
                await M.inc("dlqPush", { reason: out.status >= 500 ? "server_error" : "client_error" });
            }
            if (idem) {
                await store.idemPut(idem, out.status, out.body);
            }
            await M.inc("receipt", { outcome: out.status >= 200 && out.status < 300 ? "ok" : "error" });
            return res.status(out.status).send(out.body);
        }
        catch (e) {
            await store.dlqPush("receipt", idem || "no-idem", payload, 502, "network_error", apiKey);
            if (idem) {
                await store.idemPut(idem, 502, { error: "edge_network" });
            }
            await M.inc("dlqPush", { reason: "network_error" });
            await M.inc("receipt", { outcome: "error" });
            return res.status(502).send({ error: "edge_network" });
        }
    });
}
