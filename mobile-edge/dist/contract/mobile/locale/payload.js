export const mobileLocaleListPayload = {
    type: "object",
    required: ["locales", "default"],
    properties: {
        locales: {
            type: "array",
            items: {
                type: "object",
                required: ["code", "name", "default"],
                properties: {
                    code: { type: "string" },
                    name: { type: "string" },
                    default: { type: "boolean" },
                },
                additionalProperties: true,
            },
        },
        default: { type: "string" },
    },
    additionalProperties: true,
};
export const mobileLocaleFallbackPayload = {
    type: "object",
    required: ["locale", "chain"],
    properties: {
        locale: { type: "string" },
        chain: { type: "array", items: { type: "string" } },
    },
    additionalProperties: true,
};
export const mobileLocaleMessagePayload = {
    type: "object",
    required: ["locale", "domain", "key"],
    properties: {
        locale: { type: "string" },
        domain: { type: "string" },
        key: { type: "string" },
        message: { anyOf: [{ type: "string" }, { type: "null" }] },
        resolved_locale: { anyOf: [{ type: "string" }, { type: "null" }] },
    },
    additionalProperties: true,
};
