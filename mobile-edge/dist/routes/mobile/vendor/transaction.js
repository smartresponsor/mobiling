import { mobileAccessErrorPayload } from "../../../contract/mobile/access.js";
import { VendoringApiClient } from "../../../client/vendoring/vendoringApiClient.js";
const vendoringApiClient = new VendoringApiClient();
const mobileVendorTransactionListPayload = {
    type: "object",
    additionalProperties: false,
    required: ["vendorId", "transactions", "payload"],
    properties: {
        vendorId: { type: "string", minLength: 1 },
        transactions: {
            type: "array",
            items: {
                type: "object",
                additionalProperties: false,
                required: ["id", "status", "type", "amount", "currency", "createdAt"],
                properties: {
                    id: { anyOf: [{ type: "string", minLength: 1 }, { type: "null" }] },
                    status: { anyOf: [{ type: "string", minLength: 1 }, { type: "null" }] },
                    type: { anyOf: [{ type: "string", minLength: 1 }, { type: "null" }] },
                    amount: { type: "number" },
                    currency: { anyOf: [{ type: "string", minLength: 1 }, { type: "null" }] },
                    createdAt: { anyOf: [{ type: "string", minLength: 1 }, { type: "null" }] },
                },
            },
        },
        payload: { type: "object", additionalProperties: true },
    },
};
function forwardedHeaders(request) {
    const headers = {};
    const cookie = request.headers.cookie;
    const authorization = request.headers.authorization;
    if ("string" === typeof cookie && "" !== cookie.trim()) {
        headers.cookie = cookie.trim();
    }
    if ("string" === typeof authorization && "" !== authorization.trim()) {
        headers.authorization = authorization.trim();
    }
    return headers;
}
function isRecord(value) {
    return null !== value && "object" === typeof value && !Array.isArray(value);
}
function stringValue(value) {
    if ("string" === typeof value && "" !== value.trim()) {
        return value.trim();
    }
    if ("number" === typeof value && Number.isFinite(value)) {
        return String(value);
    }
    return null;
}
function numberValue(value) {
    if ("number" === typeof value && Number.isFinite(value)) {
        return value;
    }
    if ("string" === typeof value && "" !== value.trim()) {
        const numeric = Number(value.trim());
        return Number.isFinite(numeric) ? numeric : 0;
    }
    return 0;
}
function normalizeErrorPayload(body) {
    if (isRecord(body) && "string" === typeof body.code && "string" === typeof body.message) {
        return { code: body.code, message: body.message };
    }
    if (isRecord(body) && "string" === typeof body.error) {
        return { code: body.error, message: body.error.replace(/_/g, " ") };
    }
    return { code: "vendoring_api_error", message: "Vendoring API returned an unexpected response." };
}
function transactionPayload(body) {
    if (!isRecord(body)) {
        return {};
    }
    return isRecord(body.data) ? body.data : body;
}
function transactionList(root) {
    const direct = root.transactions ?? root.items ?? root.list;
    if (!Array.isArray(direct)) {
        return [];
    }
    return direct.filter(isRecord);
}
export default async function route(app) {
    app.get("/vendor/transaction/:vendorId", { schema: { response: { 200: mobileVendorTransactionListPayload, 400: mobileAccessErrorPayload, 404: mobileAccessErrorPayload, 422: mobileAccessErrorPayload, 500: mobileAccessErrorPayload, 503: mobileAccessErrorPayload } } }, async (request, reply) => {
        const params = request.params;
        const vendorId = stringValue(params.vendorId);
        if (null === vendorId) {
            return reply.code(400).send({ code: "invalid_vendor_id", message: "Vendor transaction request requires a vendor id." });
        }
        const result = await vendoringApiClient.getTransactionList(vendorId, forwardedHeaders(request));
        if (result.status < 200 || result.status >= 300) {
            return reply.code(result.status).send(normalizeErrorPayload(result.body));
        }
        const root = transactionPayload(result.body);
        const transactions = transactionList(root).map((transaction) => ({
            id: stringValue(transaction.id),
            status: stringValue(transaction.status),
            type: stringValue(transaction.type ?? transaction.transactionType),
            amount: numberValue(transaction.amount),
            currency: stringValue(transaction.currency),
            createdAt: stringValue(transaction.createdAt ?? transaction.created_at),
        }));
        return reply.code(200).send({
            vendorId: stringValue(root.vendorId) ?? vendorId,
            transactions,
            payload: root,
        });
    });
}
