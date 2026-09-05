import { request as httpRequest } from "http";
import { request as httpsRequest } from "https";
import { ENV } from "../../env.js";
import { resolveUpstreamBaseUrl } from "../../runtime/applicationRuntimeResolver.js";
const CATALOGING_API_UNAVAILABLE_PAYLOAD = {
    code: "cataloging_api_unavailable",
    message: "Cataloging API is unavailable from mobile-edge.",
};
export class CatalogingApiClient {
    baseUrl;
    timeoutMs;
    constructor(baseUrl = ENV.CATALOGING_API_BASE_URL, timeoutMs = ENV.CATALOGING_API_TIMEOUT_MS) {
        this.baseUrl = baseUrl;
        this.timeoutMs = timeoutMs;
    }
    get(path, forwardedHeaders = {}) {
        return this.request("GET", path, undefined, forwardedHeaders);
    }
    post(path, body, forwardedHeaders = {}) {
        return this.request("POST", path, body, forwardedHeaders);
    }
    delete(path, forwardedHeaders = {}) {
        return this.request("DELETE", path, undefined, forwardedHeaders);
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
        const transport = "https:" === url.protocol ? httpsRequest : httpRequest;
        return await new Promise((resolve) => {
            const request = transport({
                method,
                hostname: url.hostname,
                port: url.port,
                path: `${url.pathname}${url.search}`,
                headers: {
                    accept: "application/json",
                    ...(undefined === body ? {} : { "content-type": "application/json" }),
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
                    resolve({ status, body: this.parseResponseBody(text) });
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
            if (undefined !== body) {
                request.write(JSON.stringify(body));
            }
            request.end();
        });
    }
    unavailable() {
        return { status: 503, body: CATALOGING_API_UNAVAILABLE_PAYLOAD };
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
