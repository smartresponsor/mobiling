const stringField = (minLength = 1) => ({ type: "string", minLength });
const nullableStringField = (minLength = 1) => ({ anyOf: [stringField(minLength), { type: "null" }] });
const payloadField = {
    type: "object",
    additionalProperties: true,
};
const attachmentItemPayload = {
    type: "object",
    additionalProperties: false,
    required: ["attachmentId", "type", "mimeType", "downloadUrl", "payload"],
    properties: {
        attachmentId: stringField(1),
        type: stringField(1),
        mimeType: nullableStringField(1),
        downloadUrl: nullableStringField(1),
        payload: payloadField,
    },
};
export const mobileAttachmentListPayload = {
    type: "object",
    additionalProperties: false,
    required: ["ownerType", "ownerId", "count", "items", "payload"],
    properties: {
        ownerType: stringField(1),
        ownerId: stringField(1),
        count: { type: "integer", minimum: 0 },
        items: {
            type: "array",
            items: attachmentItemPayload,
        },
        payload: payloadField,
    },
};
export const mobileAttachmentLinkRequest = {
    type: "object",
    additionalProperties: false,
    required: ["attachmentId", "ownerType", "ownerId"],
    properties: {
        attachmentId: { type: "integer", minimum: 1 },
        ownerType: stringField(1),
        ownerId: stringField(1),
        context: nullableStringField(1),
        slot: nullableStringField(1),
        position: { type: "integer", minimum: 0 },
        isPrimary: { type: "boolean" },
    },
};
export const mobileAttachmentLinkPayload = {
    type: "object",
    additionalProperties: false,
    required: ["linkId", "attachmentId", "ownerType", "ownerId", "payload"],
    properties: {
        linkId: stringField(1),
        attachmentId: stringField(1),
        ownerType: stringField(1),
        ownerId: stringField(1),
        context: nullableStringField(1),
        slot: nullableStringField(1),
        position: { type: "integer", minimum: 0 },
        isPrimary: { type: "boolean" },
        payload: payloadField,
    },
};
export const mobileAttachmentDetachRequest = {
    type: "object",
    additionalProperties: false,
    required: ["attachmentId", "ownerType", "ownerId"],
    properties: {
        attachmentId: { type: "integer", minimum: 1 },
        ownerType: stringField(1),
        ownerId: stringField(1),
        context: nullableStringField(1),
        slot: nullableStringField(1),
    },
};
export const mobileAttachmentDetachPayload = {
    type: "object",
    additionalProperties: false,
    required: ["status", "attachmentId", "ownerType", "ownerId", "payload"],
    properties: {
        status: stringField(1),
        attachmentId: stringField(1),
        ownerType: stringField(1),
        ownerId: stringField(1),
        context: nullableStringField(1),
        slot: nullableStringField(1),
        payload: payloadField,
    },
};
export const mobileAttachmentFilePayload = {
    type: "object",
    additionalProperties: false,
    required: ["attachmentId", "downloadUrl", "handoffMode", "payload"],
    properties: {
        attachmentId: stringField(1),
        downloadUrl: stringField(1),
        mimeType: nullableStringField(1),
        fileName: nullableStringField(1),
        handoffMode: stringField(1),
        payload: payloadField,
    },
};
export const mobileAttachmentUploadHandoffRequest = {
    type: "object",
    additionalProperties: false,
    required: ["ownerType", "ownerId"],
    properties: {
        ownerType: stringField(1),
        ownerId: stringField(1),
        context: nullableStringField(1),
        slot: nullableStringField(1),
        isPrimary: { type: "boolean" },
        title: nullableStringField(1),
        description: nullableStringField(1),
        altText: nullableStringField(1),
    },
};
export const mobileAttachmentUploadHandoffPayload = {
    type: "object",
    additionalProperties: false,
    required: ["uploadUrl", "method", "fieldName", "form", "handoffMode", "payload"],
    properties: {
        uploadUrl: stringField(1),
        method: stringField(1),
        fieldName: stringField(1),
        form: payloadField,
        handoffMode: stringField(1),
        payload: payloadField,
    },
};
