import { request as httpRequest } from "http";
import { request as httpsRequest } from "https";
import { ENV } from "../../env.js";
import { resolveUpstreamBaseUrl } from "../../runtime/applicationRuntimeResolver.js";
const unavailablePayload = {
    code: "casing_api_unavailable",
    message: "Casing API is unavailable from mobile-edge.",
};
function forwardedHeaders(request) {
    const headers = {};
    for (const name of ["cookie", "authorization", "x-application-key", "x-application-environment"]) {
        const value = request.headers[name];
        if ("string" === typeof value && "" !== value.trim())
            headers[name] = value.trim();
    }
    return headers;
}
function parseBody(text) {
    if ("" === text.trim())
        return {};
    try {
        return JSON.parse(text);
    }
    catch {
        return { message: text };
    }
}
async function casingRequest(method, path, body, headers) {
    const baseUrl = await resolveUpstreamBaseUrl(headers, ENV.CASING_API_BASE_URL);
    if ("" === baseUrl)
        return Promise.resolve({ status: 503, body: unavailablePayload });
    let url;
    try {
        url = new URL(baseUrl.replace(/\/$/, "") + path);
    }
    catch {
        return Promise.resolve({ status: 503, body: unavailablePayload });
    }
    const payload = null === body || undefined === body ? "" : JSON.stringify(body);
    const transport = "https:" === url.protocol ? httpsRequest : httpRequest;
    return new Promise((resolve) => {
        const upstream = transport({
            method,
            hostname: url.hostname,
            port: url.port,
            path: `${url.pathname}${url.search}`,
            headers: {
                accept: "application/json",
                ...(payload ? {
                    "content-type": "application/json",
                    "content-length": Buffer.byteLength(payload).toString(),
                } : {}),
                ...headers,
            },
            timeout: ENV.CASING_API_TIMEOUT_MS,
        }, (response) => {
            const chunks = [];
            response.on("data", (chunk) => chunks.push(chunk));
            response.on("end", () => resolve({
                status: response.statusCode || 502,
                body: parseBody(Buffer.concat(chunks).toString("utf8")),
            }));
        });
        upstream.on("timeout", () => {
            upstream.destroy();
            resolve({ status: 503, body: unavailablePayload });
        });
        upstream.on("error", () => resolve({ status: 503, body: unavailablePayload }));
        if (payload)
            upstream.write(payload);
        upstream.end();
    });
}
function supportPath(requestUrl) {
    const path = requestUrl.startsWith("/") ? requestUrl : `/${requestUrl}`;
    return path === "/support" || path.startsWith("/support?") || path.startsWith("/support/") ? path : null;
}
async function proxy(request, reply) {
    const path = supportPath(request.url);
    if (null === path)
        return reply.code(404).send({ code: "support_route_not_found", message: "Support route was not found." });
    const result = await casingRequest(request.method, path, "POST" === request.method ? request.body : null, forwardedHeaders(request));
    return reply.code(result.status).send(result.body);
}
// Marketing America Corp. Oleksandr Tishchenko
export default async function route(app) {
    app.get("/support", proxy);
    app.post("/support", proxy);
    app.get("/support/*", proxy);
    app.post("/support/*", proxy);
}
