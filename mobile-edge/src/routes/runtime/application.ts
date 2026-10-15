import type { FastifyInstance } from "fastify";
import { ApplicationRuntimeResolver } from "../../runtime/applicationRuntimeResolver.js";

const runtimeResolver = new ApplicationRuntimeResolver();

interface RuntimeParams {
  applicationKey: string;
  environment: string;
}

export default async function route(app: FastifyInstance): Promise<void> {
  app.get<{ Params: RuntimeParams }>(
    "/application/runtime/:applicationKey/:environment",
    async (request, reply) => {
      const runtime = await runtimeResolver.resolve(
        request.params.applicationKey,
        request.params.environment,
      );

      if (!runtime) {
        return reply.code(503).send({
          code: "application_runtime_unavailable",
          message: "Application runtime manifest is unavailable; clients must retain their configured Mobile Edge fallback.",
        });
      }

      return reply.code(200).send(runtime);
    },
  );
}
