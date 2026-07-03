import type { FastifyInstance } from "fastify";
import { AttachingApiClient, type AttachingApiErrorPayload } from "../../client/attaching/attachingApiClient.js";
import { mobileAccessErrorPayload } from "../../contract/mobile/access/error.js";
import {
  mobileAttachmentLinkPayload,
  mobileAttachmentLinkRequest,
  mobileAttachmentListPayload,
} from "../../contract/mobile/attachment.js";

const attachingApiClient = new AttachingApiClient();

function forwardedHeaders(request: { headers: Record<string, unknown> }): Record<string, string> {
  const headers: Record<string, string> = {};
  const cookie = request.headers.cookie;
  const authorization = request.headers.authorization;

  if ("string" === typeof cookie && "" !== cookie.trim()) {
    headers.cookie = cookie.trim();
  }

  if ("string" === typeof authorization && "" !== authorization.trim()) {
    headers.authorization = authorization.trim();
  }

  return headers;
}

function isRecord(value: unknown): value is Record<string, unknown> {
  return null !== value && "object" === typeof value && !Array.isArray(value);
}

function stringValue(value: unknown): string | null {
  if ("string" === typeof value && "" !== value.trim()) {
    return value.trim();
  }

  if ("number" === typeof value && Number.isFinite(value)) {
    return String(value);
  }

  return null;
}

function integerValue(value: unknown, fallback = 0): number {
  if ("number" === typeof value && Number.isFinite(value)) {
    return Math.max(0, Math.round(value));
  }

  if ("string" === typeof value && "" !== value.trim()) {
    const numeric = Number(value.trim());

    return Number.isFinite(numeric) ? Math.max(0, Math.round(numeric)) : fallback;
  }

  return fallback;
}

function booleanValue(value: unknown, fallback = false): boolean {
  if ("boolean" === typeof value) {
    return value;
  }

  if ("string" === typeof value && "" !== value.trim()) {
    return ["1", "true", "yes", "on"].includes(value.trim().toLowerCase());
  }

  return fallback;
}

function recordValue(value: unknown): Record<string, unknown> {
  return isRecord(value) ? value : {};
}

function normalizeErrorPayload(body: unknown): AttachingApiErrorPayload {
  if (isRecord(body) && "string" === typeof body.code && "string" === typeof body.message) {
    return { code: body.code, message: body.message };
  }

  if (isRecord(body) && "string" === typeof body.error) {
    return { code: body.error, message: body.error.replace(/_/g, " ") };
  }

  return { code: "attaching_api_error", message: "Attaching API returned an unexpected response." };
}

function attachmentRoot(body: unknown): Record<string, unknown> {
  if (!isRecord(body)) {
    return {};
  }

  return isRecord(body.data) ? body.data : body;
}

function normalizeAttachmentItem(value: unknown): Record<string, unknown> {
  const item = recordValue(value);

  return {
    attachmentId: stringValue(item.attachmentId ?? item.id) ?? "attachment-unavailable",
    type: stringValue(item.type) ?? "file",
    mimeType: stringValue(item.mimeType),
    downloadUrl: stringValue(item.downloadUrl),
    payload: item,
  };
}

function normalizeAttachmentList(body: unknown): Record<string, unknown> {
  const root = attachmentRoot(body);
  const items = Array.isArray(root.items) ? root.items.map(normalizeAttachmentItem) : [];

  return {
    ownerType: stringValue(root.ownerType) ?? "unknown",
    ownerId: stringValue(root.ownerId) ?? "unknown",
    count: integerValue(root.count, items.length),
    items,
    payload: root,
  };
}

function normalizeAttachmentLink(body: unknown): Record<string, unknown> {
  const root = attachmentRoot(body);

  return {
    linkId: stringValue(root.linkId ?? root.id) ?? "attachment-link-unavailable",
    attachmentId: stringValue(root.attachmentId) ?? "attachment-unavailable",
    ownerType: stringValue(root.ownerType) ?? "unknown",
    ownerId: stringValue(root.ownerId) ?? "unknown",
    context: stringValue(root.context),
    slot: stringValue(root.slot),
    position: integerValue(root.position, 0),
    isPrimary: booleanValue(root.isPrimary, false),
    payload: root,
  };
}

function listQuery(query: unknown): { ownerType?: string; ownerId?: string; context?: string; slot?: string } {
  const source = recordValue(query);

  return {
    ownerType: stringValue(source.ownerType) ?? undefined,
    ownerId: stringValue(source.ownerId) ?? undefined,
    context: stringValue(source.context) ?? undefined,
    slot: stringValue(source.slot) ?? undefined,
  };
}

export default async function route(app: FastifyInstance): Promise<void> {
  app.get("/attachment", { schema: { response: { 200: mobileAttachmentListPayload, 400: mobileAccessErrorPayload, 404: mobileAccessErrorPayload, 422: mobileAccessErrorPayload, 500: mobileAccessErrorPayload, 503: mobileAccessErrorPayload } } }, async (request, reply) => {
    const result = await attachingApiClient.listAttachment(
      listQuery(request.query),
      forwardedHeaders(request as { headers: Record<string, unknown> }),
    );

    if (result.status < 200 || result.status >= 300) {
      return reply.code(result.status).send(normalizeErrorPayload(result.body));
    }

    return reply.code(200).send(normalizeAttachmentList(result.body));
  });

  app.post("/attachment/link", { schema: { body: mobileAttachmentLinkRequest, response: { 200: mobileAttachmentLinkPayload, 201: mobileAttachmentLinkPayload, 400: mobileAccessErrorPayload, 404: mobileAccessErrorPayload, 409: mobileAccessErrorPayload, 422: mobileAccessErrorPayload, 500: mobileAccessErrorPayload, 503: mobileAccessErrorPayload } } }, async (request, reply) => {
    const result = await attachingApiClient.attachAttachment(
      request.body,
      forwardedHeaders(request as { headers: Record<string, unknown> }),
    );

    if (result.status < 200 || result.status >= 300) {
      return reply.code(result.status).send(normalizeErrorPayload(result.body));
    }

    return reply.code(201 === result.status ? 201 : 200).send(normalizeAttachmentLink(result.body));
  });
}
