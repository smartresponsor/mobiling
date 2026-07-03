import { request as httpRequest } from "http";
import { request as httpsRequest } from "https";
import { ENV } from "../../env.js";
const ATTACHING_API_UNAVAILABLE_PAYLOAD = {
    code: "attaching_api_unavailable",
    message: "Attaching API is unavailable from mobile-edge.",
};
export class AttachingApiClient {
    baseUrl;
    timeoutMs;
    constructor(baseUrl = ENV.ATTACHING_API_BASE_URL, timeoutMs = ENV.ATTACHING_API_TIMEOUT_MS) {
        this.baseUrl = baseUrl;
        this.timeoutMs = timeoutMs;
    }
    listAttachment(query, forwardedHeaders = {}) {
        const search = new URLSearchParams();
        for (const [key, value] of Object.entries(query)) {
            if ("string" === typeof value && "" !== value.trim()) {
                search.set(key, value.trim());
            }
        }
        const suffix = "" === search.toString() ? "" : `?${search.toString()}`;
        return this.request("GET", `/attachment${suffix}`, null, forwardedHeaders);
    }
    attachAttachment(body, forwardedHeaders = {}) {
        return this.request("POST", "/attachment/attach", body, forwardedHeaders);
    }
    detachAttachment(body, forwardedHeaders = {}) {
        return this.request("POST", "/attachment/detach", body, forwardedHeaders);
    }
    attachmentFileUrl(attachmentId) {
        const baseUrl = this.baseUrl.trim();
        if ("" === baseUrl || "" === attachmentId.trim()) {
            return null;
        }
        try {
            return new URL(`${baseUrl.replace(/\/$/, "")}/attachment/${encodeURIComponent(attachmentId.trim())}/download`).toString();
        }
        catch {
            return null;
        }
    }
    attachmentUploadUrl() {
        const baseUrl = this.baseUrl.trim();
        if ("" === baseUrl) {
            return null;
        }
        try {
            return new URL(`${baseUrl.replace(/\/$/, "")}/attachment/upload`).toString();
        }
        catch {
            return null;
        }
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
            body: ATTACHING_API_UNAVAILABLE_PAYLOAD,
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
