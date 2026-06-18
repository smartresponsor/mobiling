export const analyticJson = {
    body: {
        type: "object",
        additionalProperties: false,
        required: ["events"],
        properties: {
            events: {
                type: "array",
                maxItems: 5000,
                items: {
                    type: "object",
                    required: ["type", "ts"],
                    additionalProperties: true,
                    properties: {
                        type: { type: "string" },
                        ts: { type: "number" }
                    }
                }
            }
        }
    }
};
