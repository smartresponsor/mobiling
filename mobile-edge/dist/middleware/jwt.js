import { verifyJwt } from "../auth/jwt.js";
export async function requireJwt(req) {
    const h = req.headers["authorization"];
    if (!h || !h.toLowerCase().startsWith("bearer ")) {
        const e = new Error("unauthorized");
        e.statusCode = 401;
        throw e;
    }
    const token = h.slice(7);
    try {
        req.jwt = await verifyJwt(token);
    }
    catch {
        const e = new Error("unauthorized");
        e.statusCode = 401;
        throw e;
    }
}
