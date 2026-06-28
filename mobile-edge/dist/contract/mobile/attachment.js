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
