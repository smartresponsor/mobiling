export const mobileNavigationShellPayload = {
  type: "object",
  required: ["schema", "channel", "platforms", "locations"],
  properties: {
    schema: { type: "string" },
    channel: { type: "string", enum: ["mobile"] },
    platforms: { type: "array", items: { type: "string" } },
    locations: {
      type: "object",
      additionalProperties: {
        type: "array",
        items: {
          type: "object",
          required: ["key", "label", "enabled", "visible", "status", "location"],
          properties: {
            key: { type: "string" },
            label: { type: "string" },
            icon: { anyOf: [{ type: "string" }, { type: "null" }] },
            badge: { anyOf: [{ type: "string" }, { type: "null" }] },
            enabled: { type: "boolean" },
            visible: { type: "boolean" },
            status: { type: "string" },
            disabledReason: { anyOf: [{ type: "string" }, { type: "null" }] },
            requiredComponent: { anyOf: [{ type: "string" }, { type: "null" }] },
            location: { type: "string" },
            group: { type: "string" },
            groupLabel: { type: "string" },
            action: { anyOf: [{ type: "string" }, { type: "null" }] },
            route: { anyOf: [{ type: "string" }, { type: "null" }] },
            target: { type: "object", additionalProperties: true },
            metadata: { type: "object", additionalProperties: true },
          },
          additionalProperties: true,
        },
      },
    },
  },
  additionalProperties: true,
} as const;
