const stringField = (minLength = 1) => ({ type: "string", minLength } as const);

export const mobileAccessSignInRequest = {
  body: {
    type: "object",
    additionalProperties: false,
    required: ["email", "password"],
    properties: {
      email: stringField(3),
      password: stringField(8),
    },
  },
} as const;

export const mobileAccessRegisterRequest = {
  body: {
    type: "object",
    additionalProperties: false,
    required: ["displayName", "email", "password"],
    properties: {
      displayName: stringField(1),
      email: stringField(3),
      password: stringField(8),
    },
  },
} as const;

export const mobileAccessVerificationConfirmRequest = {
  body: {
    type: "object",
    additionalProperties: false,
    required: ["code"],
    properties: {
      code: stringField(1),
    },
  },
} as const;

export const mobileAccessSecondFactorVerifyRequest = {
  body: {
    type: "object",
    additionalProperties: false,
    required: ["code"],
    properties: {
      code: stringField(1),
    },
  },
} as const;

export const mobileAccessRecoveryRequest = {
  body: {
    type: "object",
    additionalProperties: false,
    required: ["email"],
    properties: {
      email: stringField(3),
    },
  },
} as const;

export const mobileAccessRecoveryResetRequest = {
  body: {
    type: "object",
    additionalProperties: false,
    required: ["email", "code", "password"],
    properties: {
      email: stringField(3),
      code: stringField(1),
      password: stringField(8),
    },
  },
} as const;
