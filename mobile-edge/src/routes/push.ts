
import { FastifyInstance } from "fastify";
import { requireBearer } from "../middleware/bearer.js";
import { registry } from "../contract/registry.js";
import { getStore } from "../repository/index.js";

export default async function route(app: FastifyInstance){
  app.post("/push/register", {
    schema: registry.get("push.register", registry.latest("push.register")!),
    config: { rateLimit: { max: 20, timeWindow: "1 minute" } }
  }, async (req: any, res) => {
    requireBearer(req);
    const body = req.body as { deviceId:string; token:string; platform?:string };
    const store = await getStore();
    await store.deviceRegister(body.deviceId, body.token, body.platform || "android");
    return res.status(204).send();
  });
}
