import { FastifyInstance } from "fastify";
import { coreCall } from "../adapter/http/core.js";
import { adminGuard } from "../middleware/admin.js";
import * as circuit from "../service/circuit.js";

export default async function route(app: FastifyInstance): Promise<void> {
  app.post("/mobile/core/test", async (request: any) => coreCall("/test", request.body || {}));
  app.get("/admin/circuit", async (request: any) => {
    adminGuard(request);
    return circuit.status();
  });
}
