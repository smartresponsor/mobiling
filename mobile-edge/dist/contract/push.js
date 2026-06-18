export const pushRegister = {
    body: {
        type: "object",
        additionalProperties: false,
        required: ["deviceId", "token"],
        properties: {
            deviceId: { type: "string", minLength: 1 },
            token: { type: "string", minLength: 8 },
            platform: { type: "string", enum: ["android", "ios", "web"], nullable: true }
        }
    }
};
