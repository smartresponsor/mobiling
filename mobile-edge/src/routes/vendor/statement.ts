import type { FastifyInstance } from "fastify";
import { mobileAccessErrorPayload } from "../../contract/mobile/access/error.js";
import { mobileVendorStatementPayload } from "../../contract/vendor/statement.js";
import { VendoringApiClient, type VendoringApiErrorPayload } from "../../client/vendoring/vendoringApiClient.js";

const vendoringApiClient = new VendoringApiClient();

function forwardedHeaders(request: { headers: Record<string, unknown> }): Record<string, string> {
  const headers: Record<string, string> = {};
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

function isRecord(value: unknown): value is Record<string, unknown> {
  return null !== value && "object" === typeof value && !Array.isArray(value);
}

function stringValue(value: unknown): string | null {
  if ("string" === typeof value && "" !== value.trim()) {
    return value.trim();
  }

  if ("number" === typeof value && Number.isFinite(value)) {
    return String(value);
  }

  return null;
}

function numberValue(value: unknown): number {
  if ("number" === typeof value && Number.isFinite(value)) {
    return value;
  }

  if ("string" === typeof value && "" !== value.trim()) {
    const numeric = Number(value.trim());

    return Number.isFinite(numeric) ? numeric : 0;
  }

  return 0;
}

function normalizeErrorPayload(body: unknown): VendoringApiErrorPayload {
  if (isRecord(body) && "string" === typeof body.code && "string" === typeof body.message) {
    return { code: body.code, message: body.message };
  }

  if (isRecord(body) && "string" === typeof body.error) {
    return { code: body.error, message: body.error.replace(/_/g, " ") };
  }

  return { code: "vendoring_api_error", message: "Vendoring API returned an unexpected response." };
}

function statementPayload(body: unknown): Record<string, unknown> {
  if (!isRecord(body)) {
    return {};
  }

  return isRecord(body.data) ? body.data : body;
}

export default async function route(app: FastifyInstance): Promise<void> {
  app.get("/vendor/statement/:vendorId", { schema: { response: { 200: mobileVendorStatementPayload, 400: mobileAccessErrorPayload, 404: mobileAccessErrorPayload, 422: mobileAccessErrorPayload, 500: mobileAccessErrorPayload, 503: mobileAccessErrorPayload } } }, async (request, reply) => {
    const params = request.params as { vendorId?: string };
    const vendorId = stringValue(params.vendorId);

    if (null === vendorId) {
      return reply.code(400).send({ code: "invalid_vendor_id", message: "Vendor statement request requires a vendor id." });
    }

    const result = await vendoringApiClient.getStatement(vendorId, forwardedHeaders(request as { headers: Record<string, unknown> }));

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
