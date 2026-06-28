import { mobileAccessErrorPayload } from "../../../contract/mobile/access.js";
import { mobileVendorStatementPayload } from "../../../contract/mobile/vendorStatement.js";
import { VendoringApiClient } from "../../../client/vendoring/vendoringApiClient.js";
const vendoringApiClient = new VendoringApiClient();
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
function statementPayload(body) {
    if (!isRecord(body)) {
        return {};
    }
    return isRecord(body.data) ? body.data : body;
}
export default async function route(app) {
    app.get("/vendor/statement/:vendorId", { schema: { response: { 200: mobileVendorStatementPayload, 400: mobileAccessErrorPayload, 404: mobileAccessErrorPayload, 422: mobileAccessErrorPayload, 500: mobileAccessErrorPayload, 503: mobileAccessErrorPayload } } }, async (request, reply) => {
        const params = request.params;
        const vendorId = stringValue(params.vendorId);
        if (null === vendorId) {
            return reply.code(400).send({ code: "invalid_vendor_id", message: "Vendor statement request requires a vendor id." });
        }
        const result = await vendoringApiClient.getStatement(vendorId, forwardedHeaders(request));
        if (result.status < 200 || result.status >= 300) {
            return reply.code(result.status).send(normalizeErrorPayload(result.body));
        }
        const root = statementPayload(result.body);
        return reply.code(200).send({
            vendorId: stringValue(root.vendorId) ?? vendorId,
            statementStatus: stringValue(root.statementStatus ?? root.status),
            currency: stringValue(root.currency),
            grossAmount: numberValue(root.grossAmount),
            netAmount: numberValue(root.netAmount),
            payload: root,
        });
    });
}
