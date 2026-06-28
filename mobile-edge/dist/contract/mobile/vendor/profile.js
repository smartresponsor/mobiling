const stringField = (minLength = 1) => ({ type: "string", minLength });
const nullableStringField = (minLength = 1) => ({ anyOf: [stringField(minLength), { type: "null" }] });
export const mobileVendorProfilePayload = {
    type: "object",
    additionalProperties: false,
    required: [
        "vendorId",
        "displayName",
        "brandName",
        "status",
        "completionPercent",
        "readyForPublishing",
        "nextAction",
        "avatarUrl",
        "coverUrl",
        "about",
        "website",
        "publicationStatus",
    ],
    properties: {
        vendorId: stringField(1),
        displayName: nullableStringField(1),
        brandName: nullableStringField(1),
        status: nullableStringField(1),
        completionPercent: { type: "integer", minimum: 0, maximum: 100 },
        readyForPublishing: { type: "boolean" },
        nextAction: nullableStringField(1),
        avatarUrl: nullableStringField(1),
        coverUrl: nullableStringField(1),
        about: nullableStringField(1),
        website: nullableStringField(1),
        publicationStatus: nullableStringField(1),
    },
};
