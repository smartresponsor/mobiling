const stringField = (minLength = 1) => ({ type: "string", minLength } as const);
const nullableStringField = (minLength = 1) => ({ anyOf: [stringField(minLength), { type: "null" }] } as const);

const payloadField = {
  type: "object",
  additionalProperties: true,
} as const;

const catalogNodeSummaryPayload = {
  type: "object",
  additionalProperties: true,
  required: ["nodeId", "title"],
  properties: {
    nodeId: stringField(1),
    title: stringField(1),
    parentNodeId: nullableStringField(1),
  },
} as const;

export const mobileCatalogListPayload = {
  type: "object",
  additionalProperties: true,
  required: ["nodes", "payload"],
  properties: {
    nodes: { type: "array", items: catalogNodeSummaryPayload },
    payload: payloadField,
  },
} as const;

export const mobileCatalogNodeDetailPayload = {
  type: "object",
  additionalProperties: true,
  required: ["node", "payload"],
  properties: {
    node: catalogNodeSummaryPayload,
    description: nullableStringField(1),
    breadcrumbLabels: { type: "array", items: stringField(1) },
    featuredProductIds: { type: "array", items: stringField(1) },
    payload: payloadField,
  },
} as const;

export const mobileCatalogSearchPayload = {
  type: "object",
  additionalProperties: true,
  required: ["query", "nodes", "payload"],
  properties: {
    query: nullableStringField(1),
    nodes: { type: "array", items: catalogNodeSummaryPayload },
    payload: payloadField,
  },
} as const;

export const mobileCatalogMutationRequest = {
  type: "object",
  additionalProperties: true,
} as const;

export const mobileCatalogMutationPayload = {
  type: "object",
  additionalProperties: true,
  required: ["status", "payload"],
  properties: {
    status: stringField(1),
    catalogNodeId: nullableStringField(1),
    attachmentId: nullableStringField(1),
    payload: payloadField,
  },
} as const;
