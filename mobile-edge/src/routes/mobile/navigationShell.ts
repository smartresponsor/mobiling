import type { FastifyInstance } from "fastify";
import { NavigatingApiClient, type NavigatingApiErrorPayload } from "../../client/navigating/navigatingApiClient.js";
import { mobileAccessErrorPayload } from "../../contract/mobile/access/error.js";
import { mobileNavigationShellPayload } from "../../contract/mobile/navigation/shell.js";

const navigatingApiClient = new NavigatingApiClient();

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

function normalizeErrorPayload(body: unknown): NavigatingApiErrorPayload {
  if (isRecord(body) && "string" === typeof body.code && "string" === typeof body.message) {
    return {
      code: body.code,
      message: body.message,
    };
  }

  return {
    code: "navigating_api_error",
    message: "Navigating API returned an unexpected response.",
  };
}

function navigationShellSchemas() {
  return {
    shell: {
      response: {
        200: mobileNavigationShellPayload,
        500: mobileAccessErrorPayload,
        503: mobileAccessErrorPayload,
      },
    },
  } as const;
}

export default async function route(app: FastifyInstance): Promise<void> {
  const schemas = navigationShellSchemas();

  app.get("/navigation/mobile/shell", { schema: schemas.shell }, async (request, reply) => {
    const result = await navigatingApiClient.getMobileShell(
      forwardedHeaders(request as { headers: Record<string, unknown> }),
    );

    if (result.status >= 200 && result.status < 300) {
      return reply.code(200).send(result.body);
    }

    return reply.code(result.status).send(normalizeErrorPayload(result.body));
  });
}
