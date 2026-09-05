import { AttachingApiClient } from "../../client/attaching/attachingApiClient.js";
import { mobileAccessErrorPayload } from "../../contract/access/error.js";
import { mobileAttachmentDetachPayload, mobileAttachmentDetachRequest, mobileAttachmentFilePayload, mobileAttachmentLinkPayload, mobileAttachmentUploadHandoffPayload, mobileAttachmentUploadHandoffRequest, mobileAttachmentLinkRequest, mobileAttachmentListPayload, } from "../../contract/attachment/index.js";
const attachingApiClient = new AttachingApiClient();
function forwardedHeaders(request) {
    const headers = {};
    const cookie = request.headers.cookie;
    const authorization = request.headers.authorization;
    const applicationKey = request.headers["x-application-key"];
    const applicationEnvironment = request.headers["x-application-environment"];
    if ("string" === typeof cookie && "" !== cookie.trim()) {
        headers.cookie = cookie.trim();
    }
    if ("string" === typeof authorization && "" !== authorization.trim()) {
        headers.authorization = authorization.trim();
    }
    if ("string" === typeof applicationKey && "" !== applicationKey.trim()) {
        headers["x-application-key"] = applicationKey.trim();
    }
    if ("string" === typeof applicationEnvironment && "" !== applicationEnvironment.trim()) {
        headers["x-application-environment"] = applicationEnvironment.trim();
    }
    return headers;
}
function isRecord(value) {
    return null !== value && "object" === typeof value && !Array.isArray(value);
}
function stringValue(value) {
    if ("string" === typeof value && "" !== value.trim()) {
        return value.trim();
    }
    if ("number" === typeof value && Number.isFinite(value)) {
        return String(value);
    }
    return null;
}
function integerValue(value, fallback = 0) {
    if ("number" === typeof value && Number.isFinite(value)) {
        return Math.max(0, Math.round(value));
    }
    if ("string" === typeof value && "" !== value.trim()) {
        const numeric = Number(value.trim());
        return Number.isFinite(numeric) ? Math.max(0, Math.round(numeric)) : fallback;
    }
    return fallback;
}
function booleanValue(value, fallback = false) {
    if ("boolean" === typeof value) {
        return value;
    }
    if ("string" === typeof value && "" !== value.trim()) {
        return ["1", "true", "yes", "on"].includes(value.trim().toLowerCase());
    }
    return fallback;
}
function recordValue(value) {
    return isRecord(value) ? value : {};
}
function normalizeErrorPayload(body) {
    if (isRecord(body) && "string" === typeof body.code && "string" === typeof body.message) {
        return { code: body.code, message: body.message };
    }
    if (isRecord(body) && "string" === typeof body.error) {
        return { code: body.error, message: body.error.replace(/_/g, " ") };
    }
    return { code: "attaching_api_error", message: "Attaching API returned an unexpected response." };
}
function attachmentRoot(body) {
    if (!isRecord(body)) {
        return {};
    }
    return isRecord(body.data) ? body.data : body;
}
async function normalizeAttachmentItem(value, headers) {
    const item = recordValue(value);
    const attachmentId = stringValue(item.attachmentId ?? item.id) ?? "attachment-unavailable";
    return {
        attachmentId,
        type: stringValue(item.type) ?? "file",
        mediaKind: stringValue(item.mediaKind),
        documentKind: stringValue(item.documentKind),
        originalName: stringValue(item.originalName),
        title: stringValue(item.title),
        mimeType: stringValue(item.mimeType),
        extension: stringValue(item.extension),
        size: integerValue(item.size, 0),
        width: null === item.width || undefined === item.width ? null : integerValue(item.width, 0),
        height: null === item.height || undefined === item.height ? null : integerValue(item.height, 0),
        durationMs: null === item.durationMs || undefined === item.durationMs ? null : integerValue(item.durationMs, 0),
        pageCount: null === item.pageCount || undefined === item.pageCount ? null : integerValue(item.pageCount, 0),
        context: stringValue(item.context),
        slot: stringValue(item.slot),
        isPrimary: booleanValue(item.isPrimary, false),
        position: integerValue(item.position, 0),
        createdAt: stringValue(item.createdAt),
        downloadUrl: stringValue(item.downloadUrl) ?? await attachingApiClient.attachmentFileUrl(attachmentId, headers),
        payload: item,
    };
}
async function normalizeAttachmentList(body, headers) {
    const root = attachmentRoot(body);
    const items = Array.isArray(root.items) ? await Promise.all(root.items.map((item) => normalizeAttachmentItem(item, headers))) : [];
    return {
        ownerType: stringValue(root.ownerType) ?? "unknown",
        ownerId: stringValue(root.ownerId) ?? "unknown",
        count: integerValue(root.count, items.length),
        items,
        payload: root,
    };
}
function normalizeAttachmentLink(body) {
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
function normalizeAttachmentDetach(body, requestBody) {
    const root = attachmentRoot(body);
    const request = recordValue(requestBody);
    return {
        status: stringValue(root.status) ?? "detached",
        attachmentId: stringValue(root.attachmentId ?? request.attachmentId) ?? "attachment-unavailable",
        ownerType: stringValue(root.ownerType ?? request.ownerType) ?? "unknown",
        ownerId: stringValue(root.ownerId ?? request.ownerId) ?? "unknown",
        context: stringValue(root.context ?? request.context),
        slot: stringValue(root.slot ?? request.slot),
        payload: root,
    };
}
function normalizeAttachmentFile(attachmentId, downloadUrl) {
    return {
        attachmentId,
        downloadUrl,
        mimeType: null,
        fileName: null,
        handoffMode: "external_url",
        payload: { attachmentId, downloadUrl },
    };
}
function normalizeAttachmentUploadHandoff(body, uploadUrl) {
    const source = recordValue(body);
    const form = {
        ownerType: stringValue(source.ownerType) ?? "unknown",
        ownerId: stringValue(source.ownerId) ?? "unknown",
        context: stringValue(source.context),
        slot: stringValue(source.slot),
        isPrimary: booleanValue(source.isPrimary, false),
        title: stringValue(source.title),
        description: stringValue(source.description),
        altText: stringValue(source.altText),
    };
    return {
        uploadUrl,
        method: "POST",
        fieldName: "file",
        form,
        handoffMode: "multipart_direct",
        payload: { uploadUrl, form },
    };
}
function listQuery(query) {
    const source = recordValue(query);
    return {
        ownerType: stringValue(source.ownerType) ?? undefined,
        ownerId: stringValue(source.ownerId) ?? undefined,
        context: stringValue(source.context) ?? undefined,
        slot: stringValue(source.slot) ?? undefined,
    };
}
export default async function route(app) {
    app.get("/attachment", { schema: { response: { 200: mobileAttachmentListPayload, 400: mobileAccessErrorPayload, 404: mobileAccessErrorPayload, 422: mobileAccessErrorPayload, 500: mobileAccessErrorPayload, 503: mobileAccessErrorPayload } } }, async (request, reply) => {
        const headers = forwardedHeaders(request);
        const result = await attachingApiClient.listAttachment(listQuery(request.query), headers);
        if (result.status < 200 || result.status >= 300) {
            return reply.code(result.status).send(normalizeErrorPayload(result.body));
        }
        return reply.code(200).send(await normalizeAttachmentList(result.body, headers));
    });
    app.post("/attachment/link", { schema: { body: mobileAttachmentLinkRequest, response: { 200: mobileAttachmentLinkPayload, 201: mobileAttachmentLinkPayload, 400: mobileAccessErrorPayload, 404: mobileAccessErrorPayload, 409: mobileAccessErrorPayload, 422: mobileAccessErrorPayload, 500: mobileAccessErrorPayload, 503: mobileAccessErrorPayload } } }, async (request, reply) => {
        const result = await attachingApiClient.attachAttachment(request.body, forwardedHeaders(request));
        if (result.status < 200 || result.status >= 300) {
            return reply.code(result.status).send(normalizeErrorPayload(result.body));
        }
        return reply.code(201 === result.status ? 201 : 200).send(normalizeAttachmentLink(result.body));
    });
    app.post("/attachment/detach", { schema: { body: mobileAttachmentDetachRequest, response: { 200: mobileAttachmentDetachPayload, 204: mobileAttachmentDetachPayload, 400: mobileAccessErrorPayload, 404: mobileAccessErrorPayload, 409: mobileAccessErrorPayload, 422: mobileAccessErrorPayload, 500: mobileAccessErrorPayload, 503: mobileAccessErrorPayload } } }, async (request, reply) => {
        const result = await attachingApiClient.detachAttachment(request.body, forwardedHeaders(request));
        if (result.status < 200 || result.status >= 300) {
            return reply.code(result.status).send(normalizeErrorPayload(result.body));
        }
        return reply.code(200).send(normalizeAttachmentDetach(result.body, request.body));
    });
    app.get("/attachment/file/:attachmentId", { schema: { response: { 200: mobileAttachmentFilePayload, 400: mobileAccessErrorPayload, 404: mobileAccessErrorPayload, 422: mobileAccessErrorPayload, 500: mobileAccessErrorPayload, 503: mobileAccessErrorPayload } } }, async (request, reply) => {
        const params = recordValue(request.params);
        const attachmentId = stringValue(params.attachmentId);
        if (null === attachmentId) {
            return reply.code(422).send({ code: "attachment_id_required", message: "Attachment id is required." });
        }
        const downloadUrl = await attachingApiClient.attachmentFileUrl(attachmentId, forwardedHeaders(request));
        if (null === downloadUrl) {
            return reply.code(503).send({ code: "attaching_api_unavailable", message: "Attaching API is unavailable from mobile-edge." });
        }
        return reply.code(200).send(normalizeAttachmentFile(attachmentId, downloadUrl));
    });
    app.post("/attachment/upload-handoff", { schema: { body: mobileAttachmentUploadHandoffRequest, response: { 200: mobileAttachmentUploadHandoffPayload, 400: mobileAccessErrorPayload, 422: mobileAccessErrorPayload, 500: mobileAccessErrorPayload, 503: mobileAccessErrorPayload } } }, async (request, reply) => {
        const uploadUrl = await attachingApiClient.attachmentUploadUrl(forwardedHeaders(request));
        if (null === uploadUrl) {
            return reply.code(503).send({ code: "attaching_api_unavailable", message: "Attaching API is unavailable from mobile-edge." });
        }
        return reply.code(200).send(normalizeAttachmentUploadHandoff(request.body, uploadUrl));
    });
}
