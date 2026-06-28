const stringField = (minLength = 1) => ({ type: "string", minLength } as const);
const nullableStringField = (minLength = 1) => ({ anyOf: [stringField(minLength), { type: "null" }] } as const);

const payloadField = {
  type: "object",
  additionalProperties: true,
} as const;

const cartItemPayload = {
  type: "object",
  additionalProperties: false,
  required: [
    "itemId",
    "offerReference",
    "title",
    "unitPriceMinor",
    "currencyCode",
    "quantity",
    "lineTotalMinor",
    "metadata",
  ],
  properties: {
    itemId: stringField(1),
    offerReference: stringField(1),
    title: stringField(1),
    unitPriceMinor: { type: "integer", minimum: 0 },
    currencyCode: stringField(3),
    quantity: { type: "integer", minimum: 1 },
    lineTotalMinor: { type: "integer", minimum: 0 },
    metadata: payloadField,
  },
} as const;

export const mobileCartPayload = {
  type: "object",
  additionalProperties: false,
  required: [
    "cartId",
    "cartToken",
    "ownerReference",
    "status",
    "currencyCode",
    "itemCount",
    "subtotalMinor",
    "totalMinor",
    "items",
    "expiresAt",
    "updatedAt",
    "payload",
  ],
  properties: {
    cartId: nullableStringField(1),
    cartToken: stringField(1),
    ownerReference: nullableStringField(1),
    status: stringField(1),
    currencyCode: stringField(3),
    itemCount: { type: "integer", minimum: 0 },
    subtotalMinor: { type: "integer", minimum: 0 },
    totalMinor: { type: "integer", minimum: 0 },
    items: {
      type: "array",
      items: cartItemPayload,
    },
    expiresAt: nullableStringField(1),
    updatedAt: nullableStringField(1),
    payload: payloadField,
  },
} as const;

export const mobileCartItemMutationRequest = {
  type: "object",
  additionalProperties: false,
  required: ["offerReference", "quantity"],
  properties: {
    offerReference: stringField(1),
    quantity: { type: "integer", minimum: 1 },
    title: nullableStringField(1),
    unitPriceMinor: { anyOf: [{ type: "integer", minimum: 0 }, { type: "null" }] },
    currencyCode: nullableStringField(3),
    metadata: payloadField,
  },
} as const;

export const mobileCartCheckoutHandoffPayload = {
  type: "object",
  additionalProperties: false,
  required: [
    "cartId",
    "cartToken",
    "handoffId",
    "checkoutUrl",
    "status",
    "expiresAt",
    "payload",
  ],
  properties: {
    cartId: nullableStringField(1),
    cartToken: stringField(1),
    handoffId: stringField(1),
    checkoutUrl: nullableStringField(1),
    status: stringField(1),
    expiresAt: nullableStringField(1),
    payload: payloadField,
  },
} as const;
