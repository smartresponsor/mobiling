import type { FastifyInstance } from "fastify";
import { WalletingApiClient } from "../../client/walleting/walletingApiClient.js";

const client = new WalletingApiClient();

function forwarded(headers: Record<string, unknown>): Record<string, string> {
  const result: Record<string, string> = {};
  for (const key of ["cookie", "authorization"] as const) {
    const value = headers[key];
    if (typeof value === "string" && value.trim()) result[key] = value.trim();
  }
  return result;
}

export default async function route(app: FastifyInstance): Promise<void> {
  app.get("/wallet/balance", async (request, reply) => {
    const result = await client.balance(forwarded(request.headers as Record<string, unknown>));
    return reply.code(result.status).send(result.body);
  });

  app.get("/wallet/transaction", async (request, reply) => {
    const result = await client.transaction(forwarded(request.headers as Record<string, unknown>));
    return reply.code(result.status).send(result.body);
  });

  app.get("/wallet/funding", async (request, reply) => {
    const result = await client.funding(forwarded(request.headers as Record<string, unknown>));
    return reply.code(result.status).send(result.body);
  });

  app.get("/wallet/withdrawal", async (request, reply) => {
    const result = await client.withdrawal(forwarded(request.headers as Record<string, unknown>));
    return reply.code(result.status).send(result.body);
  });

  app.get<{ Params: { id: string } }>("/wallet/withdrawal/:id", async (request, reply) => {
    const result = await client.withdrawalShow(forwarded(request.headers as Record<string, unknown>), request.params.id);
    return reply.code(result.status).send(result.body);
  });

  app.get("/wallet/withdrawal/destination", async (request, reply) => {
    const result = await client.withdrawalDestination(forwarded(request.headers as Record<string, unknown>));
    return reply.code(result.status).send(result.body);
  });

  app.post("/wallet/withdrawal/request", async (request, reply) => {
    const result = await client.withdrawalRequest(forwarded(request.headers as Record<string, unknown>), request.body ?? {});
    return reply.code(result.status).send(result.body);
  });

  app.post<{ Params: { id: string } }>("/wallet/withdrawal/cancel/:id", async (request, reply) => {
    const result = await client.withdrawalCancel(forwarded(request.headers as Record<string, unknown>), request.params.id);
    return reply.code(result.status).send(result.body);
  });
}
