import { ENV } from "../env.js";
import { request as httpReq } from "http";
import { request as httpsReq } from "https";
const Q = [];
let timer = null;
function ship() {
    if (!ENV.LOG_SHIP_URL || Q.length === 0)
        return;
    const payload = Q.splice(0, 200).join("\n");
    const u = new URL(ENV.LOG_SHIP_URL);
    const lib = u.protocol === "https:" ? httpsReq : httpReq;
    const req = lib({ method: "POST", hostname: u.hostname, port: u.port, path: u.pathname + u.search, headers: { "content-type": "application/x-ndjson" } }, res => {
        res.on("data", () => { });
    });
    req.on("error", () => { });
    req.write(payload);
    req.end();
}
export function enqueue(line) {
    Q.push(line);
    if (Q.length > 1000)
        Q.shift();
    if (!timer) {
        timer = setInterval(ship, 2000);
        timer.unref();
    }
}
