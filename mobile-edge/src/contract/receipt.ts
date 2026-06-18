
export const receiptVerify = {
  headers: {
    type: "object",
    required: ["idempotency-key"],
    properties: {
      "idempotency-key": { type: "string", minLength: 8 }
    }
  },
  body: {
    type: "object",
    additionalProperties: false,
    required: ["transactionId","productId","platform","receipt"],
    properties: {
      transactionId: { type: "string", minLength: 2 },
      productId: { type: "string", minLength: 1 },
      platform: { type: "string", enum: ["android","ios"] },
      receipt: { type: "string", minLength: 8 }
    }
  }
} as const;
