import type { FastifyInstance, FastifyReply } from "fastify";
import {
  mobileAccessErrorPayload,
  mobileAccessRegisterRequest,
  mobileAccessSessionPayload,
  mobileAccessSignInRequest,
} from "../../contract/mobile/access.js";
import {
  AccessingApiClient,
  type AccessingApiErrorPayload,
  type AccessingApiSessionPayload,
} from "../../client/accessing/accessingApiClient.js";

const accessingApiClient = new AccessingApiClient();

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

function normalizeFieldErrors(fieldErrors: unknown): Record<string, string[]> | null {
  if (!isRecord(fieldErrors)) {
    return null;
  }

  const normalized: Record<string, string[]> = {};

  for (const [field, messages] of Object.entries(fieldErrors)) {
    if (!Array.isArray(messages)) {
      continue;
    }

    const normalizedMessages = messages
      .map((message) => ("string" === typeof message ? message.trim() : ""))
      .filter((message) => "" !== message);

    if (normalizedMessages.length > 0) {
      normalized[field] = normalizedMessages;
    }
  }

  return Object.keys(normalized).length > 0 ? normalized : null;
}

function normalizeIdentity(identity: unknown): AccessingApiSessionPayload["identity"] {
  if (!isRecord(identity)) {
    return null;
  }

  const vendorId = stringValue(identity.vendorId);
  const displayName = stringValue(identity.displayName);
  const email = stringValue(identity.email);

  if (null === vendorId || null === displayName || null === email) {
    return null;
  }

  return {
    vendorId,
    accountId: null,
    displayName,
    email,
    emailVerified: Boolean(identity.emailVerified),
    secondFactorEnabled: Boolean(identity.secondFactorEnabled),
  };
}

function normalizeSessionPayload(body: unknown): AccessingApiSessionPayload {
  if (!isRecord(body)) {
    return {
      status: "unauthenticated",
      identity: null,
      requiresVerification: false,
      requiresSecondFactor: false,
    };
  }

  return {
    status: "string" === typeof body.status && "" !== body.status.trim() ? body.status.trim() : "unauthenticated",
    identity: normalizeIdentity(body.identity),
    requiresVerification: Boolean(body.requiresVerification),
    requiresSecondFactor: Boolean(body.requiresSecondFactor),
  };
}

function normalizeErrorPayload(body: unknown): AccessingApiErrorPayload {
  if (isRecord(body) && "string" === typeof body.code && "string" === typeof body.message) {
    const fieldErrors = normalizeFieldErrors(body.fieldErrors);

    return {
      code: body.code,
      message: body.message,
      ...(null !== fieldErrors ? { fieldErrors } : {}),
    };
  }

  return {
    code: "accessing_api_error",
    message: "Accessing API returned an unexpected response.",
  };
}

function proxyPayload(status: number, body: unknown): AccessingApiSessionPayload | AccessingApiErrorPayload {
  if (status >= 200 && status < 300) {
    return normalizeSessionPayload(body);
  }

  return normalizeErrorPayload(body);
}

function applySessionTransport(reply: FastifyReply, responseCookie: string[]): void {
  if (responseCookie.length < 1) {
    return;
  }

  reply.header("set-cookie", responseCookie);
}

function mobileAccessRequestSchemas() {
  return {
    signin: {
      body: mobileAccessSignInRequest.body,
      response: {
        202: mobileAccessSessionPayload,
        401: mobileAccessErrorPayload,
        423: mobileAccessErrorPayload,
        429: mobileAccessErrorPayload,
        500: mobileAccessErrorPayload,
        503: mobileAccessErrorPayload,
      },
    },
    register: {
      body: mobileAccessRegisterRequest.body,
      response: {
        201: mobileAccessSessionPayload,
        409: mobileAccessErrorPayload,
        500: mobileAccessErrorPayload,
        503: mobileAccessErrorPayload,
      },
    },
    logout: {
      response: {
        200: mobileAccessSessionPayload,
        500: mobileAccessErrorPayload,
        503: mobileAccessErrorPayload,
      },
    },
    session: {
      response: {
        200: mobileAccessSessionPayload,
        500: mobileAccessErrorPayload,
        503: mobileAccessErrorPayload,
      },
    },
  } as const;
}

export default async function route(app: FastifyInstance): Promise<void> {
  const schemas = mobileAccessRequestSchemas();

  app.post("/mobile/access/signin", { schema: schemas.signin }, async (request, reply) => {
    const result = await accessingApiClient.signIn(request.body as { email: string; password: string }, forwardedHeaders(request as { headers: Record<string, unknown> }));
    applySessionTransport(reply, result.responseCookie);
    return reply.code(result.status).send(proxyPayload(result.status, result.body));
  });

  app.post("/mobile/access/register", { schema: schemas.register }, async (request, reply) => {
    const body = request.body as { displayName: string; email: string; password: string };
    const result = await accessingApiClient.register(
      {
        displayName: body.displayName,
        email: body.email,
        password: body.password,
      },
      forwardedHeaders(request as { headers: Record<string, unknown> }),
    );

    applySessionTransport(reply, result.responseCookie);
    return reply.code(result.status).send(proxyPayload(result.status, result.body));
  });

  app.post("/mobile/access/logout", { schema: schemas.logout }, async (request, reply) => {
    const result = await accessingApiClient.logout(forwardedHeaders(request as { headers: Record<string, unknown> }));
    applySessionTransport(reply, result.responseCookie);
    return reply.code(result.status).send(proxyPayload(result.status, result.body));
  });

  app.get("/mobile/access/session", { schema: schemas.session }, async (request, reply) => {
    const result = await accessingApiClient.session(forwardedHeaders(request as { headers: Record<string, unknown> }));
    applySessionTransport(reply, result.responseCookie);
    return reply.code(result.status).send(proxyPayload(result.status, result.body));
  });
}
