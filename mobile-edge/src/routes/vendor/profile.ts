import type { FastifyInstance } from "fastify";
import { mobileAccessErrorPayload } from "../../contract/access/error.js";
import { mobileVendorProfilePayload } from "../../contract/vendor/profile.js";
import { VendoringApiClient, type VendoringApiErrorPayload } from "../../client/vendoring/vendoringApiClient.js";
import { ENV } from "../../env.js";
import { resolveUpstreamBaseUrl } from "../../runtime/applicationRuntimeResolver.js";

const vendoringApiClient = new VendoringApiClient();

interface MobileVendorProfilePayload {
  vendorId: string;
  displayName: string | null;
  brandName: string | null;
  status: string | null;
  completionPercent: number;
  readyForPublishing: boolean;
  nextAction: string | null;
  avatarUrl: string | null;
  avatarAttachmentId: string | null;
  coverUrl: string | null;
  coverAttachmentId: string | null;
  canEditProfileMedia: boolean;
  about: string | null;
  website: string | null;
  publicationStatus: string | null;
}

function forwardedHeaders(request: { headers: Record<string, unknown> }): Record<string, string> {
  const headers: Record<string, string> = {};
  const cookie = request.headers.cookie;
  const authorization = request.headers.authorization;
  const applicationKey = request.headers["x-application-key"];
  const applicationEnvironment = request.headers["x-application-environment"];

  if ("string" === typeof cookie && "" !== cookie.trim()) {
    headers.cookie = cookie.trim();
  }

  if ("string" === typeof authorization && "" !== authorization.trim()) {
    headers.authorization = authorization.trim();
  }
  if ("string" === typeof applicationKey && "" !== applicationKey.trim()) {
    headers["x-application-key"] = applicationKey.trim();
  }
  if ("string" === typeof applicationEnvironment && "" !== applicationEnvironment.trim()) {
    headers["x-application-environment"] = applicationEnvironment.trim();
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

function numberValue(value: unknown): number | null {
  if ("number" === typeof value && Number.isFinite(value)) {
    return value;
  }

  if ("string" === typeof value && "" !== value.trim()) {
    const numeric = Number(value.trim());

    return Number.isFinite(numeric) ? numeric : null;
  }

  return null;
}

function nestedRecord(source: Record<string, unknown>, field: string): Record<string, unknown> {
  const value = source[field];

  return isRecord(value) ? value : {};
}

function nestedString(source: Record<string, unknown>, field: string): string | null {
  return stringValue(source[field]);
}

function absoluteVendoringUrl(value: string | null, baseUrl: string): string | null {
  if (null === value) {
    return null;
  }

  try {
    return new URL(value, baseUrl.replace(/\/$/, "") + "/").toString();
  } catch {
    return value;
  }
}

function normalizeProfile(vendorId: string, body: unknown, baseUrl: string): MobileVendorProfilePayload {
  const unwrappedBody = profilePayload(body);
  const root = isRecord(unwrappedBody) ? unwrappedBody : {};
  const profile = nestedRecord(root, "profile");
  const publicProfile = nestedRecord(root, "publicProfile");
  const publication = nestedRecord(root, "publication");
  const avatar = nestedRecord(publicProfile, "avatar");
  const cover = nestedRecord(publicProfile, "cover");
  const completion = numberValue(root.completionPercent);

  return {
    vendorId: stringValue(root.vendorId) ?? vendorId,
    displayName: nestedString(publicProfile, "displayName") ?? nestedString(profile, "displayName") ?? nestedString(publicProfile, "publicName"),
    brandName: stringValue(root.brandName) ?? nestedString(profile, "brandName"),
    status: stringValue(root.vendorStatus) ?? nestedString(profile, "vendorStatus"),
    completionPercent: null === completion ? 0 : Math.max(0, Math.min(100, Math.round(completion))),
    readyForPublishing: true === root.readyForPublishing,
    nextAction: stringValue(root.nextAction),
    avatarUrl: absoluteVendoringUrl(nestedString(avatar, "url"), baseUrl),
    avatarAttachmentId: nestedString(avatar, "attachmentId") ?? nestedString(avatar, "id"),
    coverUrl: absoluteVendoringUrl(nestedString(cover, "url"), baseUrl),
    coverAttachmentId: nestedString(cover, "attachmentId") ?? nestedString(cover, "id"),
    canEditProfileMedia: true,
    about: nestedString(publicProfile, "about") ?? nestedString(profile, "about"),
    website: nestedString(publicProfile, "website") ?? nestedString(profile, "website"),
    publicationStatus: nestedString(publication, "status") ?? nestedString(publicProfile, "status"),
  };
}

function normalizeErrorPayload(body: unknown): VendoringApiErrorPayload {
  if (isRecord(body) && "string" === typeof body.code && "string" === typeof body.message) {
    return {
      code: body.code,
      message: body.message,
    };
  }

  if (isRecord(body) && "string" === typeof body.error) {
    return {
      code: body.error,
      message: body.error.replace(/_/g, " "),
    };
  }

  return {
    code: "vendoring_api_error",
    message: "Vendoring API returned an unexpected response.",
  };
}

function vendorProfileSchemas() {
  return {
    profile: {
      response: {
        200: mobileVendorProfilePayload,
        400: mobileAccessErrorPayload,
        404: mobileAccessErrorPayload,
        422: mobileAccessErrorPayload,
        500: mobileAccessErrorPayload,
        503: mobileAccessErrorPayload,
      },
    },
  } as const;
}

export default async function route(app: FastifyInstance): Promise<void> {
  const schemas = vendorProfileSchemas();

  app.get("/vendor/profile/:vendorId", { schema: schemas.profile }, async (request, reply) => {
    const params = request.params as { vendorId?: string };
    const vendorId = stringValue(params.vendorId);

    if (null === vendorId) {
      return reply.code(400).send({
        code: "invalid_vendor_id",
        message: "Vendor profile request requires a vendor id.",
      });
    }

    const headers = forwardedHeaders(request as { headers: Record<string, unknown> });
    const result = await vendoringApiClient.getProfile(vendorId, headers);

    if (result.status >= 200 && result.status < 300) {
      const baseUrl = await resolveUpstreamBaseUrl(headers, ENV.VENDORING_API_BASE_URL);
      return reply.code(200).send(normalizeProfile(vendorId, result.body, baseUrl));
    }

    return reply.code(result.status as any).send(normalizeErrorPayload(result.body));
  });
}
function profilePayload(body: unknown): unknown {
  if (!isRecord(body)) {
    return body;
  }

  return isRecord(body.data) ? body.data : body;
}

