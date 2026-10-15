import { request as httpRequest } from "http";
import { request as httpsRequest } from "https";
import { ENV } from "../../env.js";
import { resolveUpstreamBaseUrl } from "../../runtime/applicationRuntimeResolver.js";
const LOCALIZING_API_UNAVAILABLE_PAYLOAD = {
    code: "localizing_api_unavailable",
    message: "Localizing API is unavailable from mobile-edge.",
};
export class LocalizingApiClient {
    baseUrl;
    timeoutMs;
    constructor(baseUrl = ENV.LOCALIZING_API_BASE_URL, timeoutMs = ENV.LOCALIZING_API_TIMEOUT_MS) {
        this.baseUrl = baseUrl;
        this.timeoutMs = timeoutMs;
    }
    listLocales(forwardedHeaders = {}) {
        return this.request("GET", "/api/locale", forwardedHeaders);
    }
    resolveFallback(code, forwardedHeaders = {}) {
        return this.request("GET", `/api/locale/${encodeURIComponent(code)}/fallback`, forwardedHeaders);
    }
    resolveMessage(locale, domain, key, forwardedHeaders = {}) {
        const params = new URLSearchParams({ locale, domain, key });
        return this.request("GET", `/api/locale/translation/message/resolve?${params.toString()}`, forwardedHeaders);
    }
    async request(method, path, forwardedHeaders) {
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
        const transport = "https:" === url.protocol ? httpsRequest : httpRequest;
        return await new Promise((resolve) => {
            const req = transport({
                method,
                hostname: url.hostname,
                port: url.port,
                path: `${url.pathname}${url.search}`,
                headers: {
                    accept: "application/json",
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
                    resolve({
                        status,
                        body: this.parseResponseBody(text),
                    });
                });
            });
            req.on("timeout", () => {
                try {
                    req.destroy();
                }
                catch {
                }
                resolve(this.unavailable());
            });
            req.on("error", () => resolve(this.unavailable()));
            req.end();
        });
    }
    unavailable() {
        return {
            status: 503,
            body: LOCALIZING_API_UNAVAILABLE_PAYLOAD,
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
