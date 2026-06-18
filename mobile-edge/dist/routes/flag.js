import { evalFlag } from "../service/flag.js";
const SNAP = { flags: {
        "paywall": { key: "paywall", pct: 0.3, app: "android" },
        "wifi_only": { key: "wifi_only" }
    } };
export default async function route(app) {
    app.post("/mobile/flag/eval/:key", async (req, res) => {
        const key = req.params.key;
        const ctx = req.body || {};
        const on = evalFlag(SNAP, key, ctx);
        return { key, on };
    });
}
