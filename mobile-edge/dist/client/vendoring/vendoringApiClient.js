import { request as httpRequest } from "http";
import { request as httpsRequest } from "https";
import { ENV } from "../../env.js";
const VENDORING_API_UNAVAILABLE_PAYLOAD = {
    code: "vendoring_api_unavailable",
    message: "Vendoring API is unavailable from mobile-edge.",
};
export class VendoringApiClient {
    baseUrl;
    timeoutMs;
    constructor(baseUrl = ENV.VENDORING_API_BASE_URL, timeoutMs = ENV.VENDORING_API_TIMEOUT_MS) {
        this.baseUrl = baseUrl;
        this.timeoutMs = timeoutMs;
    }
    getProfile(vendorId, forwardedHeaders = {}) {
        return this.request("GET", `/api/vendor/profile/show/${encodeURIComponent(vendorId)}`, null, forwardedHeaders);
    }
    getSummary(vendorId, forwardedHeaders = {}) {
        return this.request("GET", `/api/vendor/summary/show/${encodeURIComponent(vendorId)}`, null, forwardedHeaders);
    }
    getStatement(vendorId, forwardedHeaders = {}) {
        return this.request("GET", `/api/vendor/statement/show/${encodeURIComponent(vendorId)}`, null, forwardedHeaders);
    }
    getPayout(vendorId, forwardedHeaders = {}) {
        return this.request("GET", `/api/vendor/payout/show/${encodeURIComponent(vendorId)}`, null, forwardedHeaders);
    }
    getTransactionList(vendorId, forwardedHeaders = {}) {
        return this.request("GET", `/api/vendor/transaction/list/${encodeURIComponent(vendorId)}`, null, forwardedHeaders);
    }
    async request(method, path, body, forwardedHeaders) {
        const baseUrl = this.baseUrl.trim();
        if ("" === baseUrl) {
            return this.unavailable();
        }
        let url;
        try {
            url = new URL(baseUrl.replace(/\/$/, "") + path);
        }
        catch {
            return this.unavailable();
        }
        const payload = null === body ? "" : JSON.stringify(body);
        const transport = "https:" === url.protocol ? httpsRequest : httpRequest;
        return await new Promise((resolve) => {
            const request = transport({
                method,
                hostname: url.hostname,
                port: url.port,
                path: `${url.pathname}${url.search}`,
                headers: {
                    accept: "application/json",
                    ...(payload ? { "content-type": "application/json", "content-length": Buffer.byteLength(payload).toString() } : {}),
                    ...forwardedHeaders,
                },
                timeout: this.timeoutMs,
            }, (response) => {
                const chunks = [];
                response.on("data", (chunk) => chunks.push(chunk));
                response.on("end", () => {
                    const text = Buffer.concat(chunks).toString("utf8");
                    resolve({
                        status: response.statusCode || 502,
                        body: this.parseResponseBody(text),
                    });
                });
            });
            request.on("timeout", () => {
                try {
                    request.destroy();
                }
                catch {
                }
                resolve(this.unavailable());
            });
            request.on("error", () => resolve(this.unavailable()));
            if ("" !== payload) {
                request.write(payload);
            }
            request.end();
        });
    }
    unavailable() {
        return {
            status: 503,
            body: VENDORING_API_UNAVAILABLE_PAYLOAD,
        };
    }
    parseResponseBody(text) {
        if ("" === text.trim()) {
            return {};
        }
        try {
            return JSON.parse(text);
        }
        catch {
            return text;
        }
    }
}
