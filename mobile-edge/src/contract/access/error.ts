const stringField = (minLength = 1) => ({ type: "string", minLength } as const);

export const mobileAccessErrorPayload = {
  type: "object",
  additionalProperties: false,
  required: ["code", "message"],
  properties: {
    code: stringField(1),
    message: stringField(1),
    fieldErrors: {
      type: "object",
      additionalProperties: {
        type: "array",
        items: stringField(1),
      },
      nullable: true,
    },
  },
} as const;
