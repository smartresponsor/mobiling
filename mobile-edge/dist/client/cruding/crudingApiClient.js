import { request as httpRequest } from "http";
import { request as httpsRequest } from "https";
import { ENV } from "../../env.js";
import { resolveUpstreamBaseUrl } from "../../runtime/applicationRuntimeResolver.js";
export function crudingProblem(status, code, title, detail, resourcePath = null) {
    return {
        type: `urn:cruding:problem:${code}`,
        code,
        title,
        status,
        detail,
        errors: [],
        resourcePath,
    };
}
export class CrudingApiClient {
    baseUrl;
    timeoutMs;
    apiVersion;
    constructor(baseUrl = ENV.CRUDING_API_BASE_URL, timeoutMs = ENV.CRUDING_API_TIMEOUT_MS, apiVersion = ENV.CRUDING_API_VERSION) {
        this.baseUrl = baseUrl;
        this.timeoutMs = timeoutMs;
        this.apiVersion = apiVersion;
    }
    request(method, resource, identity, body, headers) {
        const suffix = identity ? `/${encodeURIComponent(identity)}` : "";
        const path = method === "POST" && resource === "project" && !identity
            ? "/api/project/wizard"
            : `/api/${encodeURIComponent(this.apiVersion)}/${encodeURIComponent(resource)}${suffix}`;
        return this.requestPath(method, path, body, headers);
    }
    capabilities(resource, headers) {
        return this.requestPath("GET", `/api/${encodeURIComponent(this.apiVersion)}/_crud/${encodeURIComponent(resource)}/capabilities`, null, headers);
    }
    async requestPath(method, path, body, headers) {
        const baseUrl = await resolveUpstreamBaseUrl(headers, this.baseUrl);
        if (!baseUrl)
            return Promise.resolve({ status: 503, body: crudingProblem(503, "cruding_api_unavailable", "Service Unavailable", "Cruding API is unavailable.") });
        let url;
        try {
            url = new URL(baseUrl.replace(/\/$/, "") + path);
        }
        catch {
            return Promise.resolve({ status: 503, body: crudingProblem(503, "cruding_api_unavailable", "Service Unavailable", "Cruding API URL is invalid.") });
        }
        const payload = body == null ? "" : JSON.stringify(body);
        const transport = url.protocol === "https:" ? httpsRequest : httpRequest;
        return new Promise((resolve) => {
            const req = transport({ method, hostname: url.hostname, port: url.port, path: `${url.pathname}${url.search}`, headers: {
                    accept: "application/json",
                    ...(payload ? { "content-type": "application/json", "content-length": String(Buffer.byteLength(payload)) } : {}),
                    ...headers,
                }, timeout: this.timeoutMs }, (response) => {
                const chunks = [];
                response.on("data", (chunk) => chunks.push(chunk));
                response.on("end", () => {
                    const text = Buffer.concat(chunks).toString("utf8");
                    let parsed = {};
                    if (text.trim())
                        try {
                            parsed = JSON.parse(text);
                        }
                        catch {
                            parsed = { message: text };
                        }
                    resolve({ status: response.statusCode || 502, body: parsed });
                });
            });
            req.on("timeout", () => { req.destroy(); resolve({ status: 503, body: crudingProblem(503, "cruding_api_timeout", "Service Unavailable", "Cruding API request timed out.") }); });
            req.on("error", () => resolve({ status: 503, body: crudingProblem(503, "cruding_api_unavailable", "Service Unavailable", "Cruding API request failed.") }));
            if (payload)
                req.write(payload);
            req.end();
        });
    }
}
