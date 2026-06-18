import { ENV } from "../env.js";
export function adminGuard(req) {
    const ip = (req.ip || "").replace("::ffff:", "");
    if (ENV.ADMIN_ALLOW_IPS.length && !ENV.ADMIN_ALLOW_IPS.includes(ip)) {
        const e = new Error("forbidden");
        e.statusCode = 403;
        throw e;
    }
    if (req.headers["x-admin-token"] !== ENV.ADMIN_TOKEN) {
        const e = new Error("forbidden");
        e.statusCode = 403;
        throw e;
    }
    if (ENV.CSRF_REQUIRED) {
        const hdr = String(req.headers["x-csrf-token"] || "");
        const cook = String((req.headers["cookie"] || "").split("csrf=")[1] || "").split(";")[0];
        if (!hdr || !cook || hdr !== cook) {
            const e = new Error("csrf");
            e.statusCode = 403;
            throw e;
        }
    }
}
