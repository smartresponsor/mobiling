const stringField = (minLength = 1) => ({ type: "string", minLength });
const nullableStringField = (minLength = 1) => ({ anyOf: [stringField(minLength), { type: "null" }] });
const payloadField = {
    type: "object",
    additionalProperties: true,
};
const catalogNodeSummaryPayload = {
    type: "object",
    additionalProperties: true,
    required: ["nodeId", "title"],
    properties: {
        nodeId: stringField(1),
        title: stringField(1),
        parentNodeId: nullableStringField(1),
    },
};
export const mobileCatalogListPayload = {
    type: "object",
    additionalProperties: true,
    required: ["nodes", "payload"],
    properties: {
        nodes: { type: "array", items: catalogNodeSummaryPayload },
        payload: payloadField,
    },
};
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
};
export const mobileCatalogSearchPayload = {
    type: "object",
    additionalProperties: true,
    required: ["query", "nodes", "payload"],
    properties: {
        query: nullableStringField(1),
        nodes: { type: "array", items: catalogNodeSummaryPayload },
        payload: payloadField,
    },
};
