const stringField = (minLength = 1) => ({ type: "string", minLength } as const);

export const mobileLocaleErrorPayload = {
  type: "object",
  additionalProperties: false,
  required: ["code", "message"],
  properties: {
    code: stringField(1),
    message: stringField(1),
  },
} as const;
