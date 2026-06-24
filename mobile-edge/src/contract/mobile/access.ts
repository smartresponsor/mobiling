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
    required: ["code", "password"],
    properties: {
      code: stringField(1),
      password: stringField(8),
    },
  },
} as const;

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
} as const;

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
} as const;

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
