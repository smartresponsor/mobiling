import type { FastifyInstance } from "fastify";
import {
  mobileAccessErrorPayload,
  mobileAccessRegisterRequest,
  mobileAccessSessionPayload,
  mobileAccessSignInRequest,
} from "../../contract/mobile/access.js";

const CONTRACT_ONLY_MESSAGE =
  "Mobile access API contract is materialized, but Accessing integration is not connected in this milestone.";

function contractOnlyError(code: string) {
  return {
    code,
    message: CONTRACT_ONLY_MESSAGE,
  };
}

export default async function route(app: FastifyInstance): Promise<void> {
  app.post(
    "/mobile/access/signin",
    {
      schema: {
        body: mobileAccessSignInRequest.body,
        response: {
          501: mobileAccessErrorPayload,
        },
      },
    },
    async (_request, reply) => reply.code(501).send(contractOnlyError("mobile_access_contract_only"))
  );

  app.post(
    "/mobile/access/register",
    {
      schema: {
        body: mobileAccessRegisterRequest.body,
        response: {
          501: mobileAccessErrorPayload,
        },
      },
    },
    async (_request, reply) => reply.code(501).send(contractOnlyError("mobile_access_contract_only"))
  );

  app.post(
    "/mobile/access/logout",
    {
      schema: {
        response: {
          501: mobileAccessErrorPayload,
        },
      },
    },
    async (_request, reply) => reply.code(501).send(contractOnlyError("mobile_access_contract_only"))
  );

  app.get(
    "/mobile/access/session",
    {
      schema: {
        response: {
          200: mobileAccessSessionPayload,
          501: mobileAccessErrorPayload,
        },
      },
    },
    async () => ({
      status: "unauthenticated",
      identity: null,
      accessToken: null,
      refreshToken: null,
      expiresAt: null,
      requiresVerification: false,
      requiresSecondFactor: false,
    })
  );
}
