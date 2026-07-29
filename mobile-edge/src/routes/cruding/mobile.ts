import type { FastifyInstance } from "fastify";
import { CrudingApiClient } from "../../client/cruding/crudingApiClient.js";

const client = new CrudingApiClient();
const allowed = new Set(["product", "order", "project"]);

function forwarded(headers: Record<string, unknown>): Record<string, string> {
  const result: Record<string, string> = {};
  for (const key of ["cookie", "authorization"] as const) {
    const value = headers[key];
    if (typeof value === "string" && value.trim()) result[key] = value.trim();
  }
  return result;
}

export default async function route(app: FastifyInstance): Promise<void> {
  app.route({
    method: ["GET", "POST", "PUT", "PATCH", "DELETE"],
    url: "/crud/my/:resource/:identity?",
    handler: async (request, reply) => {
      const params = request.params as { resource?: string; identity?: string };
      const resource = (params.resource || "").trim();
      if (!allowed.has(resource)) return reply.code(404).send({ code: "crud_resource_not_allowed", message: "Mobile CRUD resource is not allowed." });
      const result = await client.request(
        request.method,
        resource,
        params.identity?.trim() || null,
        request.body ?? null,
        forwarded(request.headers as Record<string, unknown>),
      );
      return reply.code(result.status).send(result.body);
    },
  });
}
