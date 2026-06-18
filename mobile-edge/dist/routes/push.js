import { requireBearer } from "../middleware/bearer.js";
import { registry } from "../contract/registry.js";
import { getStore } from "../repository/index.js";
export default async function route(app) {
    app.post("/mobile/push/register", {
        schema: registry.get("push.register", registry.latest("push.register")),
        config: { rateLimit: { max: 20, timeWindow: "1 minute" } }
    }, async (req, res) => {
        requireBearer(req);
        const body = req.body;
        const store = await getStore();
        await store.deviceRegister(body.deviceId, body.token, body.platform || "android");
        return res.status(204).send();
    });
}
