import { request as httpRequest } from "http";
import { request as httpsRequest } from "https";
import { mobileAccessErrorPayload } from "../../contract/access/error.js";
import { ENV } from "../../env.js";
import { resolveUpstreamBaseUrl } from "../../runtime/applicationRuntimeResolver.js";
const unavailablePayload = { code: "notifying_api_unavailable", message: "Notifying API is unavailable from mobile-edge." };
function isRecord(value) {
    return null !== value && "object" === typeof value && !Array.isArray(value);
}
function stringValue(value) {
    return "string" === typeof value && "" !== value.trim() ? value.trim() : null;
}
function integerValue(value) {
    const numeric = "number" === typeof value ? value : Number(value);
    return Number.isFinite(numeric) ? Math.max(0, Math.round(numeric)) : 0;
}
function forwardedHeaders(request) {
    const headers = {};
    for (const name of ["cookie", "authorization", "x-notifying-recipient-key", "x-application-key", "x-application-environment"]) {
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
        return text;
    }
}
async function notifyingRequest(method, path, body, headers) {
    const baseUrl = await resolveUpstreamBaseUrl(headers, ENV.NOTIFYING_API_BASE_URL);
    if ("" === baseUrl)
        return Promise.resolve({ status: 503, body: unavailablePayload });
    let url;
    try {
        url = new URL(baseUrl.replace(/\/$/, "") + path);
    }
    catch {
        return Promise.resolve({ status: 503, body: unavailablePayload });
    }
    const payload = null === body ? "" : JSON.stringify(body);
    const transport = "https:" === url.protocol ? httpsRequest : httpRequest;
    return new Promise((resolve) => {
        const request = transport({
            method,
            hostname: url.hostname,
            port: url.port,
            path: `${url.pathname}${url.search}`,
            headers: { accept: "application/json", ...(payload ? { "content-type": "application/json", "content-length": Buffer.byteLength(payload).toString() } : {}), ...headers },
            timeout: ENV.NOTIFYING_API_TIMEOUT_MS,
        }, (response) => {
            const chunks = [];
            response.on("data", (chunk) => chunks.push(chunk));
            response.on("end", () => resolve({ status: response.statusCode || 502, body: parseBody(Buffer.concat(chunks).toString("utf8")) }));
        });
        request.on("timeout", () => { request.destroy(); resolve({ status: 503, body: unavailablePayload }); });
        request.on("error", () => resolve({ status: 503, body: unavailablePayload }));
        if (payload)
            request.write(payload);
        request.end();
    });
}
function normalizeError(body) {
    if (isRecord(body) && "string" === typeof body.message)
        return { code: stringValue(body.code) ?? "notifying_api_error", message: body.message };
    return { code: "notifying_api_error", message: "Notifying API returned an unexpected response." };
}
function normalizeItem(value) {
    const item = isRecord(value) ? value : {};
    return {
        id: stringValue(item.id) ?? "notification-unavailable",
        notificationId: stringValue(item.notificationId) ?? "notification-unavailable",
        status: stringValue(item.status) ?? "new",
        title: stringValue(item.title) ?? "Notification",
        body: stringValue(item.body) ?? "",
        priority: stringValue(item.priority) ?? "normal",
        actionUrl: stringValue(item.actionUrl),
        createdAt: stringValue(item.createdAt) ?? new Date(0).toISOString(),
        readAt: stringValue(item.readAt),
    };
}
const inboxPayload = { type: "object", required: ["items", "unreadCount"], properties: { items: { type: "array" }, unreadCount: { type: "integer", minimum: 0 } }, additionalProperties: true };
const unreadPayload = { type: "object", required: ["unreadCount"], properties: { unreadCount: { type: "integer", minimum: 0 } }, additionalProperties: true };
const markReadRequest = { type: "object", required: ["ids"], properties: { ids: { type: "array", items: { type: "string", minLength: 1 } } }, additionalProperties: false };
const subscriptionRequest = {
    type: "object",
    required: ["platform", "appKey", "deviceId", "token", "enabled"],
    properties: {
        platform: { type: "string", minLength: 1 },
        appKey: { type: "string", minLength: 1 },
        deviceId: { type: "string", minLength: 1 },
        token: { type: "string", minLength: 1 },
        enabled: { type: "boolean" },
    },
    additionalProperties: true,
};
// Marketing America Corp. Oleksandr Tishchenko
export default async function route(app) {
    app.get("/notification", { schema: { response: { 200: inboxPayload, 403: mobileAccessErrorPayload, 503: mobileAccessErrorPayload } } }, async (request, reply) => {
        const headers = forwardedHeaders(request);
        const [inbox, unread] = await Promise.all([
            notifyingRequest("GET", "/api/notification/inbox?limit=50&offset=0", null, headers),
            notifyingRequest("GET", "/api/notification/unread/count", null, headers),
        ]);
        if (inbox.status < 200 || inbox.status >= 300)
            return reply.code(inbox.status).send(normalizeError(inbox.body));
        if (unread.status < 200 || unread.status >= 300)
            return reply.code(unread.status).send(normalizeError(unread.body));
        const inboxRoot = isRecord(inbox.body) ? inbox.body : {};
        const unreadRoot = isRecord(unread.body) ? unread.body : {};
        const items = Array.isArray(inboxRoot.items) ? inboxRoot.items.map(normalizeItem) : [];
        return reply.code(200).send({ items, unreadCount: integerValue(unreadRoot.unreadCount) });
    });
    app.get("/notification/unread/count", { schema: { response: { 200: unreadPayload, 403: mobileAccessErrorPayload, 503: mobileAccessErrorPayload } } }, async (request, reply) => {
        const result = await notifyingRequest("GET", "/api/notification/unread/count", null, forwardedHeaders(request));
        if (result.status < 200 || result.status >= 300)
            return reply.code(result.status).send(normalizeError(result.body));
        const root = isRecord(result.body) ? result.body : {};
        return reply.code(200).send({ unreadCount: integerValue(root.unreadCount) });
    });
    app.post("/notification/mark/read", { schema: { body: markReadRequest, response: { 200: unreadPayload, 403: mobileAccessErrorPayload, 503: mobileAccessErrorPayload } } }, async (request, reply) => {
        const root = isRecord(request.body) ? request.body : {};
        const ids = Array.isArray(root.ids) ? root.ids.filter((id) => "string" === typeof id && "" !== id.trim()) : [];
        const headers = forwardedHeaders(request);
        const result = await notifyingRequest("POST", "/api/notification/mark/read", { recipientEntryIds: ids }, headers);
        if (result.status < 200 || result.status >= 300)
            return reply.code(result.status).send(normalizeError(result.body));
        const unread = await notifyingRequest("GET", "/api/notification/unread/count", null, headers);
        if (unread.status < 200 || unread.status >= 300)
            return reply.code(unread.status).send(normalizeError(unread.body));
        const unreadRoot = isRecord(unread.body) ? unread.body : {};
        return reply.code(200).send({ unreadCount: integerValue(unreadRoot.unreadCount) });
    });
    app.post("/notification/subscription", { schema: { body: subscriptionRequest, response: { 200: { type: "object", additionalProperties: true }, 400: mobileAccessErrorPayload, 403: mobileAccessErrorPayload, 503: mobileAccessErrorPayload } } }, async (request, reply) => {
        const headers = forwardedHeaders(request);
        const body = isRecord(request.body) ? request.body : {};
        const result = await notifyingRequest("POST", "/api/notification/subscription", body, headers);
        if (result.status < 200 || result.status >= 300)
            return reply.code(result.status).send(normalizeError(result.body));
        return reply.code(200).send(isRecord(result.body) ? result.body : { ok: true });
    });
}
