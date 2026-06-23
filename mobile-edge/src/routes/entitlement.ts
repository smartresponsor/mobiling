
import { FastifyInstance } from "fastify";
import { requireBearer } from "../middleware/bearer.js";

export default async function route(app: FastifyInstance){
  app.get("/entitlement", { config: { rateLimit: { max: 30, timeWindow: "1 minute" } } }, async (req, res) => {
    requireBearer(req);
    return { grants: [{ code: "pro", ttl: 3600 }, { code: "feature_x", ttl: 1200 }] };
  });
}
