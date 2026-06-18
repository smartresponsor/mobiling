import { signJwt, verifyJwt } from "../auth/jwt.js";
export default async function route(app) {
    app.post("/mobile/session", async (_req, _res) => {
        const token = await signJwt({ sub: "user:dev", scope: ["mobile"] });
        const now = Math.floor(Date.now() / 1000);
        return { token, token_type: "Bearer", expires_at: now + 3600 };
    });
    app.post("/mobile/session/refresh", async (req, res) => {
        const h = String(req.headers["authorization"] || "");
        if (!h.toLowerCase().startsWith("bearer "))
            return res.status(401).send({ error: "unauthorized" });
        try {
            await verifyJwt(h.slice(7));
        }
        catch {
            return res.status(401).send({ error: "unauthorized" });
        }
        const token = await signJwt({ sub: "user:dev", scope: ["mobile"] });
        const now = Math.floor(Date.now() / 1000);
        return { token, token_type: "Bearer", expires_at: now + 3600 };
    });
}
