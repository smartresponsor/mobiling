import { ENV } from "../env.js";
export function requireBearer(req) {
    const h = req.headers["authorization"];
    if (!h || h !== ENV.DEV_BEARER) {
        const e = new Error("unauthorized");
        e.statusCode = 401;
        throw e;
    }
}
