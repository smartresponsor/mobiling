import { request as httpRequest } from "http";
import { request as httpsRequest } from "https";
import type { FastifyInstance } from "fastify";
import { mobileAccessErrorPayload } from "../../contract/access/error.js";
import { ENV } from "../../env.js";

interface MessagingApiResponse {
  status: number;
  body: unknown;
}

const unavailablePayload = {
  code: "messaging_api_unavailable",
  message: "Messaging API is unavailable from mobile-edge.",
};

function forwardedHeaders(request: { headers: Record<string, unknown> }): Record<string, string> {
  const headers: Record<string, string> = {};
  const cookie = request.headers.cookie;
  const authorization = request.headers.authorization;
  const userId = request.headers["x-user-id"];
  const powNonce = request.headers["x-pow-nonce"];
  const powTs = request.headers["x-pow-ts"];

  if ("string" === typeof cookie && "" !== cookie.trim()) headers.cookie = cookie.trim();
  if ("string" === typeof authorization && "" !== authorization.trim()) headers.authorization = authorization.trim();
  if ("string" === typeof userId && "" !== userId.trim()) headers["x-user-id"] = userId.trim();
  if ("string" === typeof powNonce && "" !== powNonce.trim()) headers["x-pow-nonce"] = powNonce.trim();
  if ("string" === typeof powTs && "" !== powTs.trim()) headers["x-pow-ts"] = powTs.trim();

  return headers;
}

function isRecord(value: unknown): value is Record<string, unknown> {
  return null !== value && "object" === typeof value && !Array.isArray(value);
}

function recordValue(value: unknown): Record<string, unknown> {
  return isRecord(value) ? value : {};
}

function stringValue(value: unknown): string | null {
  if ("string" === typeof value && "" !== value.trim()) return value.trim();
  if ("number" === typeof value && Number.isFinite(value)) return String(value);

  return null;
}

function headerString(request: { headers: Record<string, unknown> }, name: string): string | null {
  return stringValue(request.headers[name.toLowerCase()] ?? request.headers[name]);
}

function integerValue(value: unknown, fallback = 0): number {
  if ("number" === typeof value && Number.isFinite(value)) return Math.max(0, Math.round(value));
  if ("string" === typeof value && "" !== value.trim()) {
    const numeric = Number(value.trim());

    return Number.isFinite(numeric) ? Math.max(0, Math.round(numeric)) : fallback;
  }

  return fallback;
}

function parseBody(text: string): unknown {
  if ("" === text.trim()) return {};

  try {
    return JSON.parse(text);
  } catch {
    return text;
  }
}

function messagingRequest(method: string, path: string, body: unknown, headers: Record<string, string>): Promise<MessagingApiResponse> {
  const baseUrl = ENV.MESSAGING_API_BASE_URL.trim();
  if ("" === baseUrl) return Promise.resolve({ status: 503, body: unavailablePayload });

  let url: URL;
  try {
    url = new URL(baseUrl.replace(/\/$/, "") + path);
  } catch {
    return Promise.resolve({ status: 503, body: unavailablePayload });
  }

  const payload = null === body ? "" : JSON.stringify(body);
  const transport = "https:" === url.protocol ? httpsRequest : httpRequest;

  return new Promise<MessagingApiResponse>((resolve) => {
    const request = transport(
      {
        method,
        hostname: url.hostname,
        port: url.port,
        path: `${url.pathname}${url.search}`,
        headers: {
          accept: "application/json",
          ...(payload ? { "content-type": "application/json", "content-length": Buffer.byteLength(payload).toString() } : {}),
          ...headers,
        },
        timeout: ENV.MESSAGING_API_TIMEOUT_MS,
      },
      (response) => {
        const chunks: Buffer[] = [];
        response.on("data", (chunk: Buffer) => chunks.push(chunk));
        response.on("end", () => {
          const status = response.statusCode || 502;
          if (status >= 300 && status < 400) {
            resolve({ status: 503, body: unavailablePayload });
            return;
          }
          resolve({ status, body: parseBody(Buffer.concat(chunks).toString("utf8")) });
        });
      },
    );

    request.on("timeout", () => {
      try {
        request.destroy();
      } catch {
      }
      resolve({ status: 503, body: unavailablePayload });
    });
    request.on("error", () => resolve({ status: 503, body: unavailablePayload }));
    if ("" !== payload) request.write(payload);
    request.end();
  });
}

function normalizeErrorPayload(body: unknown): Record<string, string> {
  if (isRecord(body) && "string" === typeof body.code && "string" === typeof body.message) {
    return { code: body.code, message: body.message };
  }
  if (isRecord(body) && "string" === typeof body.error) {
    return { code: body.error, message: body.error.replace(/_/g, " ") };
  }

  return { code: "messaging_api_error", message: "Messaging API returned an unexpected response." };
}

function payloadRoot(body: unknown): Record<string, unknown> {
  if (!isRecord(body)) return {};

  return isRecord(body.data) ? body.data : body;
}

function normalizeThread(value: unknown): Record<string, unknown> {
  const item = recordValue(value);

  return {
    threadId: stringValue(item.threadId ?? item.id) ?? "thread-unavailable",
    subject: stringValue(item.subject ?? item.title),
    lastMessagePreview: stringValue(item.lastMessagePreview ?? item.preview ?? item.lastMessage ?? item.content) ?? "",
    unreadCount: integerValue(item.unreadCount, 0),
    updatedAtIso8601: stringValue(item.updatedAtIso8601 ?? item.updatedAt ?? item.createdAt) ?? new Date(0).toISOString(),
    payload: item,
  };
}

function normalizeMessage(value: unknown, fallbackThreadId: string): Record<string, unknown> {
  const item = recordValue(value);

  return {
    messageId: stringValue(item.messageId ?? item.id) ?? "message-unavailable",
    threadId: stringValue(item.threadId ?? item.thread_id) ?? fallbackThreadId,
    body: stringValue(item.body ?? item.content ?? item.text) ?? "",
    senderId: stringValue(item.senderId ?? item.senderUserId ?? item.sender) ?? "sender-unavailable",
    sentAtIso8601: stringValue(item.sentAtIso8601 ?? item.createdAt ?? item.sentAt) ?? new Date(0).toISOString(),
    payload: item,
  };
}

const threadListPayload = {
  type: "object",
  required: ["items", "count"],
  properties: { items: { type: "array" }, count: { type: "integer", minimum: 0 }, payload: { type: "object", additionalProperties: true } },
  additionalProperties: true,
} as const;

const messageListPayload = {
  type: "object",
  required: ["threadId", "items", "count"],
  properties: { threadId: { type: "string", minLength: 1 }, items: { type: "array" }, count: { type: "integer", minimum: 0 }, payload: { type: "object", additionalProperties: true } },
  additionalProperties: true,
} as const;

const messageSendRequest = {
  type: "object",
  required: ["body"],
  properties: { body: { type: "string", minLength: 1 } },
  additionalProperties: true,
} as const;

const messageItemPayload = {
  type: "object",
  required: ["messageId", "threadId", "body", "senderId", "sentAtIso8601"],
  properties: {
    messageId: { type: "string", minLength: 1 },
    threadId: { type: "string", minLength: 1 },
    body: { type: "string" },
    senderId: { type: "string", minLength: 1 },
    sentAtIso8601: { type: "string", minLength: 1 },
    payload: { type: "object", additionalProperties: true },
  },
  additionalProperties: true,
} as const;

// Marketing America Corp. Oleksandr Tishchenko
export default async function route(app: FastifyInstance): Promise<void> {
  app.get("/message/thread", { schema: { response: { 200: threadListPayload, 400: mobileAccessErrorPayload, 403: mobileAccessErrorPayload, 500: mobileAccessErrorPayload, 503: mobileAccessErrorPayload } } }, async (request, reply) => {
    const result = await messagingRequest("GET", "/api/threads", null, forwardedHeaders(request as { headers: Record<string, unknown> }));
    if (result.status < 200 || result.status >= 300) return reply.code(result.status as any).send(normalizeErrorPayload(result.body));

    const root = payloadRoot(result.body);
    const items = Array.isArray(root.items) ? root.items : Array.isArray(root.threads) ? root.threads : [];

    return reply.code(200).send({ items: items.map(normalizeThread), count: integerValue(root.count, items.length), payload: root });
  });

  app.get("/message/thread/:threadId", { schema: { response: { 200: messageListPayload, 400: mobileAccessErrorPayload, 403: mobileAccessErrorPayload, 404: mobileAccessErrorPayload, 500: mobileAccessErrorPayload, 503: mobileAccessErrorPayload } } }, async (request, reply) => {
    const threadId = stringValue(recordValue(request.params).threadId);
    if (null === threadId) return reply.code(400).send({ code: "thread_id_required", message: "Thread id is required." });

    const search = new URLSearchParams({ limit: "50" });
    const result = await messagingRequest("GET", `/api/threads/${encodeURIComponent(threadId)}/messages?${search.toString()}`, null, forwardedHeaders(request as { headers: Record<string, unknown> }));
    if (result.status < 200 || result.status >= 300) return reply.code(result.status as any).send(normalizeErrorPayload(result.body));

    const root = payloadRoot(result.body);
    const items = Array.isArray(root.items) ? root.items : Array.isArray(root.messages) ? root.messages : [];

    return reply.code(200).send({ threadId: stringValue(root.threadId) ?? threadId, items: items.map((item) => normalizeMessage(item, threadId)), count: integerValue(root.count, items.length), payload: root });
  });

  app.post("/message/thread/:threadId/send", { schema: { body: messageSendRequest, response: { 200: messageItemPayload, 201: messageItemPayload, 400: mobileAccessErrorPayload, 403: mobileAccessErrorPayload, 429: mobileAccessErrorPayload, 500: mobileAccessErrorPayload, 503: mobileAccessErrorPayload } } }, async (request, reply) => {
    const threadId = stringValue(recordValue(request.params).threadId);
    const requestRecord = request as { headers: Record<string, unknown> };
    const requestBody = recordValue(request.body);
    const body = stringValue(requestBody.body);
    const userId = stringValue(requestBody.userId) ?? headerString(requestRecord, "x-user-id");
    if (null === threadId) return reply.code(400).send({ code: "thread_id_required", message: "Thread id is required." });
    if (null === body) return reply.code(400).send({ code: "message_body_required", message: "Message body is required." });
    if (null === userId) return reply.code(400).send({ code: "user_id_required", message: "User id is required." });

    const result = await messagingRequest("POST", `/api/threads/${encodeURIComponent(threadId)}/reply`, { userId, text: body }, forwardedHeaders(requestRecord));
    if (result.status < 200 || result.status >= 300) return reply.code(result.status as any).send(normalizeErrorPayload(result.body));

    return reply.code(201 === result.status ? 201 : 200).send(normalizeMessage(payloadRoot(result.body), threadId));
  });
}
