import { ENV } from "../env.js";
export default async function route(app) {
    app.post("/admin/secret/rotate", async (req, res) => {
        if (req.headers["x-admin-token"] !== ENV.ADMIN_TOKEN)
            return res.status(403).send({ error: "forbidden" });
        const { name, value, graceSec = 0 } = req.body || {};
        if (!name || !value)
            return res.status(400).send({ error: "bad_request" });
        if (name === "JWT_SECRET") {
            ENV.JWT_PREV_SECRET = ENV.JWT_SECRET;
            ENV.JWT_PREV_EXP_TS = Date.now() + Math.max(0, Number(graceSec)) * 1000;
            ENV.JWT_SECRET = String(value);
            return { ok: true, name, graceSec };
        }
        if (name === "WEBHOOK_SECRET") {
            ENV.WEBHOOK_SECRET = String(value);
            return { ok: true, name };
        }
        return res.status(400).send({ error: "unsupported_secret" });
    });
}
