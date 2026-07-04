const stringField = (minLength = 1) => ({ type: "string", minLength } as const);
const nullableStringField = (minLength = 1) => ({ anyOf: [stringField(minLength), { type: "null" }] } as const);

export const mobileVendorStatementPayload = {
  type: "object",
  additionalProperties: false,
  required: [
    "vendorId",
    "statementStatus",
    "currency",
    "grossAmount",
    "netAmount",
    "payload",
  ],
  properties: {
    vendorId: stringField(1),
    statementStatus: nullableStringField(1),
    currency: nullableStringField(1),
    grossAmount: { type: "number" },
    netAmount: { type: "number" },
    payload: {
      type: "object",
      additionalProperties: true,
    },
  },
} as const;
