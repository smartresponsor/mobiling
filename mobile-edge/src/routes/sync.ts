
import { FastifyInstance } from "fastify";
export default async function route(app: FastifyInstance){
  app.post("/sync/event", { config: { rateLimit: { max: 120, timeWindow: "1 minute" } } }, async (_req, res) => res.status(204).send());
}
