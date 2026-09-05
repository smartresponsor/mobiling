import { request as httpRequest } from "http";
import { request as httpsRequest } from "https";
import { ENV } from "../../env.js";
import { resolveUpstreamBaseUrl } from "../../runtime/applicationRuntimeResolver.js";
export class WalletingApiClient {
    baseUrl;
    timeoutMs;
    constructor(baseUrl = ENV.CRUDING_API_BASE_URL, timeoutMs = ENV.CRUDING_API_TIMEOUT_MS) {
        this.baseUrl = baseUrl;
        this.timeoutMs = timeoutMs;
    }
    balance(headers) {
        return this.get("/api/wallet/balance", headers);
    }
    transaction(headers) {
        return this.get("/api/wallet/transaction", headers);
    }
    funding(headers) {
        return this.get("/api/wallet/funding", headers);
    }
    withdrawal(headers) {
        return this.get("/api/wallet/withdrawal", headers);
    }
    withdrawalShow(headers, id) {
        return this.get(`/api/wallet/withdrawal/${encodeURIComponent(id)}`, headers);
    }
    withdrawalDestination(headers) {
        return this.get("/api/wallet/withdrawal/destination", headers);
    }
    withdrawalRequest(headers, body) {
        return this.send("POST", "/api/wallet/withdrawal/request", headers, body);
    }
    withdrawalCancel(headers, id) {
        return this.send("POST", `/api/wallet/withdrawal/cancel/${encodeURIComponent(id)}`, headers, {});
    }
    get(path, headers) {
        return this.send("GET", path, headers);
    }
    async send(method, path, headers, body) {
        const baseUrl = await resolveUpstreamBaseUrl(headers, this.baseUrl);
        if (!baseUrl)
            return Promise.resolve({ status: 503, body: { code: "walleting_api_unavailable", message: "Walleting API is unavailable." } });
        let url;
        try {
            url = new URL(baseUrl.replace(/\/$/, "") + path);
        }
        catch {
            return Promise.resolve({ status: 503, body: { code: "walleting_api_unavailable", message: "Walleting API URL is invalid." } });
        }
        const transport = url.protocol === "https:" ? httpsRequest : httpRequest;
        const payload = method === "POST" ? JSON.stringify(body ?? {}) : null;
        return new Promise((resolve) => {
            const req = transport({
                method,
                hostname: url.hostname,
                port: url.port,
                path: url.pathname,
                headers: {
                    accept: "application/json",
                    ...(payload !== null ? { "content-type": "application/json", "content-length": Buffer.byteLength(payload).toString() } : {}),
                    ...headers,
                },
                timeout: this.timeoutMs,
            }, (response) => {
                const chunks = [];
                response.on("data", (chunk) => chunks.push(chunk));
                response.on("end", () => {
                    const text = Buffer.concat(chunks).toString("utf8");
                    let body = {};
                    if (text.trim())
                        try {
                            body = JSON.parse(text);
                        }
                        catch {
                            body = { message: text };
                        }
                    resolve({ status: response.statusCode || 502, body });
                });
            });
            req.on("timeout", () => { req.destroy(); resolve({ status: 503, body: { code: "walleting_api_timeout", message: "Walleting API request timed out." } }); });
            req.on("error", () => resolve({ status: 503, body: { code: "walleting_api_unavailable", message: "Walleting API request failed." } }));
            if (payload !== null)
                req.write(payload);
            req.end();
        });
    }
}
