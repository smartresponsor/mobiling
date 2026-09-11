import { ENV } from "../../env.js";
import { resolveUpstreamBaseUrl } from "../../runtime/applicationRuntimeResolver.js";
export class CurrencingApiClient {
    baseUrl;
    timeoutMs;
    constructor(baseUrl = ENV.CURRENCING_API_BASE_URL, timeoutMs = ENV.CURRENCING_API_TIMEOUT_MS) {
        this.baseUrl = baseUrl;
        this.timeoutMs = timeoutMs;
    }
    async get(path, forwardedHeaders = {}) {
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
        try {
            const response = await fetch(url, {
                method: "GET",
                headers: { accept: "application/json", ...forwardedHeaders },
                signal: AbortSignal.timeout(this.timeoutMs),
                redirect: "manual",
            });
            if (response.status >= 300 && response.status < 400) {
                return this.unavailable();
            }
            const text = await response.text();
            let body = {};
            if ("" !== text.trim()) {
                try {
                    body = JSON.parse(text);
                }
                catch {
                    body = text;
                }
            }
            return { status: response.status, body };
        }
        catch {
            return this.unavailable();
        }
    }
    unavailable() {
        return { status: 503, body: { code: "currencing_api_unavailable", message: "Currencing API is unavailable from mobile-edge." } };
    }
}
