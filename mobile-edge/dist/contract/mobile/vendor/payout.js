const stringField = (minLength = 1) => ({ type: "string", minLength });
const nullableStringField = (minLength = 1) => ({ anyOf: [stringField(minLength), { type: "null" }] });
export const mobileVendorPayoutPayload = {
    type: "object",
    additionalProperties: false,
    required: [
        "vendorId",
        "payoutStatus",
        "currency",
        "availableAmount",
        "pendingAmount",
        "payoutAccountLabel",
        "payload",
    ],
    properties: {
        vendorId: stringField(1),
        payoutStatus: nullableStringField(1),
        currency: nullableStringField(1),
        availableAmount: { type: "number" },
        pendingAmount: { type: "number" },
        payoutAccountLabel: nullableStringField(1),
        payload: {
            type: "object",
            additionalProperties: true,
        },
    },
};
