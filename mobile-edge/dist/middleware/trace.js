import { randomUUID } from "crypto";
import { ENV } from "../env.js";
export async function traceMw(req, res) {
    const id = req.headers["x-trace-id"] || randomUUID();
    req.traceId = id;
    res.header(ENV.TRACE_RESPONSE_HEADER, id);
}
