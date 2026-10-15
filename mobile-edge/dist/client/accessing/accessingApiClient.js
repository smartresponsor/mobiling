import { request as httpRequest } from "http";
import { request as httpsRequest } from "https";
import { ENV } from "../../env.js";
import { ApplicationRuntimeResolver } from "../../runtime/applicationRuntimeResolver.js";
const ACCESSING_API_UNAVAILABLE_PAYLOAD = {
    code: "accessing_api_unavailable",
    message: "Accessing API is unavailable from mobile-edge.",
};
export class AccessingApiClient {
    baseUrl;
    timeoutMs;
    runtimeResolver = new ApplicationRuntimeResolver();
    constructor(baseUrl = ENV.ACCESSING_API_BASE_URL, timeoutMs = ENV.ACCESSING_API_TIMEOUT_MS) {
        this.baseUrl = baseUrl;
        this.timeoutMs = timeoutMs;
    }
    signIn(request, forwardedHeaders = {}) {
        return this.request("POST", "/api/access/signin", request, forwardedHeaders);
    }
    register(request, forwardedHeaders = {}) {
        return this.request("POST", "/api/access/register", request, forwardedHeaders);
    }
    logout(forwardedHeaders = {}) {
        return this.request("POST", "/api/access/logout", null, forwardedHeaders);
    }
    session(forwardedHeaders = {}) {
        return this.request("GET", "/api/access/session", null, forwardedHeaders);
    }
    resendVerification(forwardedHeaders = {}) {
        return this.request("POST", "/api/access/verification/resend", null, forwardedHeaders);
    }
    confirmVerification(request, forwardedHeaders = {}) {
        return this.request("POST", "/api/access/verification/confirm", request, forwardedHeaders);
    }
    challengeSecondFactor(forwardedHeaders = {}) {
        return this.request("POST", "/api/access/second-factor/challenge", null, forwardedHeaders);
    }
    verifySecondFactor(request, forwardedHeaders = {}) {
        return this.request("POST", "/api/access/second-factor/verify", request, forwardedHeaders);
    }
    requestRecovery(request, forwardedHeaders = {}) {
        return this.request("POST", "/api/access/recovery/request", request, forwardedHeaders);
    }
    resetRecovery(request, forwardedHeaders = {}) {
        return this.request("POST", "/api/access/recovery/reset", request, forwardedHeaders);
    }
    async request(method, path, body, forwardedHeaders) {
        const applicationKey = forwardedHeaders["x-application-key"] || "";
        const applicationEnvironment = forwardedHeaders["x-application-environment"] || "";
        const runtime = await this.runtimeResolver.resolve(applicationKey, applicationEnvironment);
        const baseUrl = (runtime?.effectiveOrigin || this.baseUrl).trim();
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
                    const responseCookie = response.headers["set-cookie"] || [];
                    resolve({
                        status: response.statusCode || 502,
                        body: this.parseResponseBody(text),
                        responseCookie: Array.isArray(responseCookie) ? responseCookie : [responseCookie],
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
            body: ACCESSING_API_UNAVAILABLE_PAYLOAD,
            responseCookie: [],
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
