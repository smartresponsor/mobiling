import { adminGuard } from "../middleware/admin.js";
import { getStore } from "../repository/index.js";
export default async function route(app) {
    app.get("/admin/apikey", async (req) => {
        adminGuard(req);
        const s = await getStore();
        return await s.apikeyList();
    });
    app.post("/admin/apikey", async (req, res) => {
        adminGuard(req);
        const { key, name, quota_rpm, enabled, requeue_rpm, requeue_daily } = req.body || {};
        if (!key || !name)
            return res.status(400).send({ error: "bad_request" });
        const s = await getStore();
        await s.apikeyUpsert(String(key), String(name), Number(quota_rpm || 0), !!enabled, requeue_rpm != null ? Number(requeue_rpm) : null, requeue_daily != null ? Number(requeue_daily) : null);
        return { ok: true };
    });
}
