const stringField = (minLength = 1) => ({ type: "string", minLength } as const);
const nullableStringField = (minLength = 1) => ({ anyOf: [stringField(minLength), { type: "null" }] } as const);

const currencyMetadata = {
  type: "object",
  additionalProperties: false,
  required: ["code", "numericCode", "minorUnit", "symbol", "displayName"],
  properties: {
    code: stringField(3),
    numericCode: nullableStringField(3),
    minorUnit: { type: "integer", minimum: 0, maximum: 8 },
    symbol: nullableStringField(1),
    displayName: nullableStringField(1),
  },
} as const;

const currencyChoice = {
  type: "object",
  additionalProperties: false,
  required: ["code", "label", "symbol", "minorUnit"],
  properties: {
    code: stringField(3),
    label: stringField(1),
    symbol: nullableStringField(1),
    minorUnit: { type: "integer", minimum: 0, maximum: 8 },
  },
} as const;

export const mobileCurrencyCatalogPayload = {
  type: "object",
  additionalProperties: false,
  required: ["items"],
  properties: {
    items: { type: "array", items: currencyMetadata },
  },
} as const;

export const mobileCurrencySelectorPayload = {
  type: "object",
  additionalProperties: false,
  required: ["choices", "selectedCode", "placeholder"],
  properties: {
    choices: { type: "array", items: currencyChoice },
    selectedCode: nullableStringField(3),
    placeholder: stringField(1),
  },
} as const;

