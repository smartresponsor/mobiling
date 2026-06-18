import { ENV } from "../env.js";
import { hmacHex } from "../util/hmac.js";
export function verifyAnalyticSignature(req, body) {
    const sig = req.headers["x-sr-analytic-signature"];
    if (!sig || typeof sig !== "string") {
        const e = new Error("missing signature");
        e.statusCode = 400;
        throw e;
    }
    const calc = hmacHex(ENV.ANALYTIC_SECRET, body);
    if (calc.toLowerCase() !== String(sig).toLowerCase()) {
        const e = new Error("bad signature");
        e.statusCode = 401;
        throw e;
    }
}
