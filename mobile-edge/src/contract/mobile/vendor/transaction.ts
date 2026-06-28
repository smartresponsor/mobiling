const stringField = (minLength = 1) => ({ type: "string", minLength } as const);
const nullableStringField = (minLength = 1) => ({ anyOf: [stringField(minLength), { type: "null" }] } as const);

export const mobileVendorTransactionListPayload = {
  type: "object",
  additionalProperties: false,
  required: ["vendorId", "transactions", "payload"],
  properties: {
    vendorId: stringField(1),
    transactions: {
      type: "array",
      items: {
        type: "object",
        additionalProperties: false,
        required: [
          "id",
          "status",
          "type",
          "amount",
          "currency",
          "createdAt",
        ],
        properties: {
          id: nullableStringField(1),
          status: nullableStringField(1),
          type: nullableStringField(1),
          amount: { type: "number" },
          currency: nullableStringField(1),
          createdAt: nullableStringField(1),
        },
      },
    },
    payload: {
      type: "object",
      additionalProperties: true,
    },
  },
} as const;
