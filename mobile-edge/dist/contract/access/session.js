const stringField = (minLength = 1) => ({ type: "string", minLength });
export const mobileAccessIdentityPayload = {
    type: "object",
    additionalProperties: false,
    required: ["vendorId", "accountId", "displayName", "email", "emailVerified", "secondFactorEnabled"],
    properties: {
        vendorId: stringField(1),
        accountId: { anyOf: [stringField(1), { type: "null" }] },
        displayName: stringField(1),
        email: stringField(3),
        emailVerified: { type: "boolean" },
        secondFactorEnabled: { type: "boolean" },
    },
};
export const mobileAccessSessionPayload = {
    type: "object",
    additionalProperties: false,
    required: [
        "status",
        "identity",
        "requiresVerification",
        "requiresSecondFactor",
    ],
    properties: {
        status: stringField(1),
        identity: { ...mobileAccessIdentityPayload, nullable: true },
        requiresVerification: { type: "boolean" },
        requiresSecondFactor: { type: "boolean" },
    },
};
