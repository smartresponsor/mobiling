const stringField = (minLength = 1) => ({ type: "string", minLength });
export const mobileLocaleErrorPayload = {
    type: "object",
    additionalProperties: false,
    required: ["code", "message"],
    properties: {
        code: stringField(1),
        message: stringField(1),
    },
};
