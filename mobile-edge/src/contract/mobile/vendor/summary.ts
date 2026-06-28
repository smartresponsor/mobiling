const stringField = (minLength = 1) => ({ type: "string", minLength } as const);
const nullableStringField = (minLength = 1) => ({ anyOf: [stringField(minLength), { type: "null" }] } as const);

export const mobileVendorSummaryPayload = {
  type: "object",
  additionalProperties: false,
  required: [
    "vendorId",
    "brandName",
    "status",
    "profileCompletionPercent",
    "nextAction",
    "payload",
  ],
  properties: {
    vendorId: stringField(1),
    brandName: nullableStringField(1),
    status: nullableStringField(1),
    profileCompletionPercent: { type: "integer", minimum: 0, maximum: 100 },
    nextAction: nullableStringField(1),
    payload: {
      type: "object",
      additionalProperties: true,
    },
  },
} as const;
