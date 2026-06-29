import type { FastifyInstance } from "fastify";
import { LocalizingApiClient, type LocalizingApiErrorPayload } from "../../../client/localizing/localizingApiClient.js";
import { mobileLocaleErrorPayload } from "../../../contract/mobile/locale/error.js";
import { mobileLocaleFallbackPayload, mobileLocaleListPayload, mobileLocaleMessagePayload } from "../../../contract/mobile/locale/payload.js";

const localizingApiClient = new LocalizingApiClient();

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

function normalizeErrorPayload(body: unknown): LocalizingApiErrorPayload {
  if (isRecord(body) && "string" === typeof body.code && "string" === typeof body.message) {
    return { code: body.code, message: body.message };
  }

  return { code: "localizing_api_error", message: "Localizing API returned an unexpected response." };
}

function mobileLocaleSchemas() {
  return {
    list: {
      response: {
        200: mobileLocaleListPayload,
        503: mobileLocaleErrorPayload,
      },
    },
    fallback: {
      response: {
        200: mobileLocaleFallbackPayload,
        400: mobileLocaleErrorPayload,
        503: mobileLocaleErrorPayload,
      },
    },
    message: {
      response: {
        200: mobileLocaleMessagePayload,
        400: mobileLocaleErrorPayload,
        404: mobileLocaleErrorPayload,
        503: mobileLocaleErrorPayload,
      },
    },
  } as const;
}

export default async function routeMobileLocale(app: FastifyInstance): Promise<void> {
  const schemas = mobileLocaleSchemas();

  app.get("/locale", { schema: schemas.list }, async (request, reply) => {
    const result = await localizingApiClient.listLocales(
      forwardedHeaders(request as { headers: Record<string, unknown> }),
    );

    if (result.status >= 200 && result.status < 300) {
      return reply.code(200).send(result.body);
    }

    return reply.code(result.status).send(normalizeErrorPayload(result.body));
  });

  app.get("/locale/:code/fallback", { schema: schemas.fallback }, async (request, reply) => {
    const { code } = request.params as { code: string };

    if ("" === code.trim()) {
      return reply.code(400).send({ code: "invalid_locale_code", message: "Locale code is required." });
    }

    const result = await localizingApiClient.resolveFallback(
      code.trim(),
      forwardedHeaders(request as { headers: Record<string, unknown> }),
    );

    if (result.status >= 200 && result.status < 300) {
      return reply.code(200).send(normalizeFallbackPayload(result.body));
    }

    return reply.code(result.status).send(normalizeErrorPayload(result.body));
  });

  app.get("/locale/translation/message/resolve", { schema: schemas.message }, async (request, reply) => {
    const query = request.query as Record<string, string>;
    const locale = (query.locale || "").trim();
    const domain = (query.domain || "").trim();
    const key = (query.key || "").trim();

    if ("" === locale || "" === domain || "" === key) {
      return reply.code(400).send({ code: "invalid_params", message: "locale, domain and key query params are required." });
    }

    const result = await localizingApiClient.resolveMessage(
      locale,
      domain,
      key,
      forwardedHeaders(request as { headers: Record<string, unknown> }),
    );

    if (result.status >= 200 && result.status < 300) {
      return reply.code(200).send(result.body);
    }

    return reply.code(result.status).send(normalizeErrorPayload(result.body));
  });
}
function normalizeFallbackPayload(body: unknown): unknown {
  if (!isRecord(body)) {
    return body;
  }

  if (Array.isArray(body.fallback_chain) && !Object.prototype.hasOwnProperty.call(body, "chain")) {
    return { ...body, chain: body.fallback_chain };
  }

  return body;
}

