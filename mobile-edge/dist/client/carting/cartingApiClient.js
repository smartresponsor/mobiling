import { request as httpRequest } from "http";
import { request as httpsRequest } from "https";
import { ENV } from "../../env.js";
import { resolveUpstreamBaseUrl } from "../../runtime/applicationRuntimeResolver.js";
const CARTING_API_UNAVAILABLE_PAYLOAD = {
    code: "carting_api_unavailable",
    message: "Carting API is unavailable from mobile-edge.",
};
export class CartingApiClient {
    baseUrl;
    timeoutMs;
    constructor(baseUrl = ENV.CARTING_API_BASE_URL, timeoutMs = ENV.CARTING_API_TIMEOUT_MS) {
        this.baseUrl = baseUrl;
        this.timeoutMs = timeoutMs;
    }
    getCurrentCart(forwardedHeaders = {}) {
        return this.request("GET", "/api/cart/current", null, forwardedHeaders);
    }
    addItem(body, forwardedHeaders = {}) {
        return this.request("POST", "/api/cart/item", body, forwardedHeaders);
    }
    prepareCheckoutHandoff(forwardedHeaders = {}) {
        return this.request("POST", "/api/cart/checkout-handoff", null, forwardedHeaders);
    }
    async request(method, path, body, forwardedHeaders) {
        const baseUrl = await resolveUpstreamBaseUrl(forwardedHeaders, this.baseUrl);
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
                    const status = response.statusCode || 502;
                    if (status >= 300 && status < 400) {
                        resolve(this.unavailable());
                        return;
                    }
                    const text = Buffer.concat(chunks).toString("utf8");
                    const cartTokenHeader = response.headers["x-cart-token"];
                    resolve({
                        status,
                        body: this.parseResponseBody(text),
                        ...(typeof cartTokenHeader === "string" && cartTokenHeader.trim() !== "" ? { cartToken: cartTokenHeader.trim() } : {}),
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
            body: CARTING_API_UNAVAILABLE_PAYLOAD,
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
