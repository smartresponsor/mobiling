import { request as httpRequest } from "http";
import { request as httpsRequest } from "https";
import { mobileAccessErrorPayload } from "../../contract/access/error.js";
import { ENV } from "../../env.js";
import { resolveUpstreamBaseUrl } from "../../runtime/applicationRuntimeResolver.js";
const unavailablePayload = { code: "notifying_api_unavailable", message: "Notifying API is unavailable from mobile-edge." };
const classifierUnavailablePayload = { code: "need_classifier_unavailable", message: "Need classifier is unavailable from mobile-edge." };
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
function optionalNumberValue(value) {
    const numeric = "number" === typeof value ? value : Number(value);
    return Number.isFinite(numeric) ? numeric : null;
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
async function classifierRequest(body) {
    const baseUrl = ENV.ONE_TASKER_CLASSIFIER_API_BASE_URL.trim();
    if ("" === baseUrl)
        return Promise.resolve({ status: 503, body: classifierUnavailablePayload });
    let url;
    try {
        url = new URL(baseUrl.replace(/\/$/, "") + "/need/classify");
    }
    catch {
        return Promise.resolve({ status: 503, body: classifierUnavailablePayload });
    }
    const payload = JSON.stringify(body);
    const transport = "https:" === url.protocol ? httpsRequest : httpRequest;
    const headers = {
        accept: "application/json",
        "content-type": "application/json",
        "content-length": Buffer.byteLength(payload).toString(),
    };
    if ("" !== ENV.ONE_TASKER_CLASSIFIER_AUTHORIZATION.trim())
        headers.authorization = ENV.ONE_TASKER_CLASSIFIER_AUTHORIZATION.trim();
    return new Promise((resolve) => {
        const request = transport({
            method: "POST",
            hostname: url.hostname,
            port: url.port,
            path: `${url.pathname}${url.search}`,
            headers,
            timeout: ENV.ONE_TASKER_CLASSIFIER_API_TIMEOUT_MS,
        }, (response) => {
            const chunks = [];
            response.on("data", (chunk) => chunks.push(chunk));
            response.on("end", () => resolve({ status: response.statusCode || 502, body: parseBody(Buffer.concat(chunks).toString("utf8")) }));
        });
        request.on("timeout", () => { request.destroy(); resolve({ status: 503, body: classifierUnavailablePayload }); });
        request.on("error", () => resolve({ status: 503, body: classifierUnavailablePayload }));
        request.write(payload);
        request.end();
    });
}
function normalizeError(body) {
    if (isRecord(body) && "string" === typeof body.message)
        return { code: stringValue(body.code) ?? "notifying_api_error", message: body.message };
    return { code: "notifying_api_error", message: "Notifying API returned an unexpected response." };
}
function normalizeClassifierError(body) {
    if (isRecord(body) && "string" === typeof body.message)
        return { code: stringValue(body.code) ?? "need_classifier_error", message: body.message };
    return { code: "need_classifier_error", message: "Need classifier returned an unexpected response." };
}
function normalizeItem(value) {
    const item = isRecord(value) ? value : {};
    const payload = isRecord(item.payload) ? item.payload : {};
    return {
        id: stringValue(item.id) ?? "notification-unavailable",
        notificationId: stringValue(item.notificationId) ?? "notification-unavailable",
        status: stringValue(item.status) ?? "new",
        sourceComponent: stringValue(item.sourceComponent) ?? "unknown",
        eventName: stringValue(item.eventName) ?? "unknown",
        topic: stringValue(item.topic) ?? "default",
        title: stringValue(item.title) ?? "Notification",
        body: stringValue(item.body) ?? "",
        priority: stringValue(item.priority) ?? "normal",
        actionUrl: stringValue(item.actionUrl),
        createdAt: stringValue(item.createdAt) ?? new Date(0).toISOString(),
        readAt: stringValue(item.readAt),
        needCategory: stringValue(payload.category),
        needConfidence: optionalNumberValue(payload.confidence),
        needLocale: stringValue(payload.locale),
        needClassifierVersion: stringValue(payload.classifierVersion),
    };
}
const inboxPayload = { type: "object", required: ["items", "unreadCount"], properties: { items: { type: "array" }, unreadCount: { type: "integer", minimum: 0 } }, additionalProperties: true };
const unreadPayload = { type: "object", required: ["unreadCount"], properties: { unreadCount: { type: "integer", minimum: 0 } }, additionalProperties: true };
const markReadRequest = { type: "object", required: ["ids"], properties: { ids: { type: "array", items: { type: "string", minLength: 1 } } }, additionalProperties: false };
const recognizedNeedRequest = {
    type: "object",
    required: ["summary", "category", "source"],
    properties: {
        summary: { type: "string", minLength: 1, maxLength: 500 },
        category: { type: "string", minLength: 1, maxLength: 120 },
        confidence: { type: "number", minimum: 0, maximum: 1 },
        source: { type: "string", minLength: 1, maxLength: 64 },
        locale: { type: "string", maxLength: 32 },
        classifierVersion: { type: "string", maxLength: 120 },
        correlationId: { type: "string", maxLength: 190 },
    },
    additionalProperties: false,
};
const classifyNeedRequest = {
    type: "object",
    required: ["transcript"],
    properties: {
        transcript: { type: "string", minLength: 1, maxLength: 1200 },
        locale: { type: "string", maxLength: 32 },
    },
    additionalProperties: false,
};
const classifyNeedResponse = {
    type: "object",
    required: ["isNeed", "category", "summary", "confidence", "classifierVersion"],
    properties: {
        isNeed: { type: "boolean" },
        category: { anyOf: [{ type: "string", minLength: 1, maxLength: 120 }, { type: "null" }] },
        summary: { anyOf: [{ type: "string", minLength: 1, maxLength: 500 }, { type: "null" }] },
        confidence: { type: "number", minimum: 0, maximum: 1 },
        classifierVersion: { type: "string", minLength: 1, maxLength: 120 },
    },
    additionalProperties: false,
};
const callbackRequest = {
    type: "object",
    required: ["recipientEntryId"],
    properties: { recipientEntryId: { type: "string", minLength: 1, maxLength: 190 } },
    additionalProperties: false,
};
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
    app.post("/need/recognized", { schema: { body: recognizedNeedRequest, response: { 201: { type: "object", additionalProperties: true }, 400: mobileAccessErrorPayload, 403: mobileAccessErrorPayload, 503: mobileAccessErrorPayload } } }, async (request, reply) => {
        const headers = forwardedHeaders(request);
        const body = isRecord(request.body) ? request.body : {};
        const result = await notifyingRequest("POST", "/api/notification/need/recognized", body, headers);
        if (result.status < 200 || result.status >= 300)
            return reply.code(result.status).send(normalizeError(result.body));
        return reply.code(201).send(isRecord(result.body) ? result.body : { ok: true });
    });
    app.post("/need/classify", { schema: { body: classifyNeedRequest, response: { 200: classifyNeedResponse, 400: mobileAccessErrorPayload, 502: mobileAccessErrorPayload, 503: mobileAccessErrorPayload } } }, async (request, reply) => {
        const body = isRecord(request.body) ? request.body : {};
        const transcript = stringValue(body.transcript);
        if (null === transcript)
            return reply.code(400).send({ code: "invalid_transcript", message: "Transcript is required." });
        const locale = stringValue(body.locale);
        const result = await classifierRequest({ transcript, ...(locale ? { locale } : {}) });
        if (result.status < 200 || result.status >= 300)
            return reply.code(result.status).send(normalizeClassifierError(result.body));
        const root = isRecord(result.body) ? result.body : {};
        const isNeed = "boolean" === typeof root.isNeed ? root.isNeed : null;
        const category = stringValue(root.category);
        const summary = stringValue(root.summary);
        const confidence = Number(root.confidence);
        const classifierVersion = stringValue(root.classifierVersion);
        if (null === isNeed || !Number.isFinite(confidence) || null === classifierVersion || (isNeed && (null === category || null === summary))) {
            return reply.code(502).send({ code: "need_classifier_invalid_response", message: "Need classifier returned an invalid structured result." });
        }
        return reply.code(200).send({
            isNeed,
            category,
            summary,
            confidence: Math.min(1, Math.max(0, confidence)),
            classifierVersion,
        });
    });
    app.post("/need/callback", { schema: { body: callbackRequest, response: { 201: { type: "object", additionalProperties: true }, 404: mobileAccessErrorPayload, 409: mobileAccessErrorPayload, 403: mobileAccessErrorPayload, 503: mobileAccessErrorPayload } } }, async (request, reply) => {
        const headers = forwardedHeaders(request);
        const body = isRecord(request.body) ? request.body : {};
        const result = await notifyingRequest("POST", "/api/notification/need/callback", body, headers);
        if (result.status < 200 || result.status >= 300)
            return reply.code(result.status).send(normalizeError(result.body));
        return reply.code(201).send(isRecord(result.body) ? result.body : { ok: true });
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
