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

export const mobileAccessIdentityPayload = {
  type: "object",
  additionalProperties: false,
  required: ["userId", "accountId", "displayName", "email", "emailVerified", "secondFactorEnabled"],
  properties: {
    userId: stringField(1),
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
    "accessToken",
    "refreshToken",
    "expiresAt",
    "requiresVerification",
    "requiresSecondFactor",
  ],
  properties: {
    status: stringField(1),
    identity: { ...mobileAccessIdentityPayload, nullable: true },
    accessToken: { type: "string", minLength: 1, nullable: true },
    refreshToken: { type: "string", minLength: 1, nullable: true },
    expiresAt: { type: "string", minLength: 1, nullable: true },
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
