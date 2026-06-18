
import { FastifyInstance } from "fastify";
import { ENV } from "../env.js";
export default async function route(app: FastifyInstance){
  app.post("/admin/secret/rotate", async (req:any, res)=>{
    if (req.headers["x-admin-token"] !== ENV.ADMIN_TOKEN) return res.status(403).send({error:"forbidden"});
    const { name, value, graceSec=0 } = req.body || {};
    if (!name || !value) return res.status(400).send({error:"bad_request"});
    if (name === "JWT_SECRET"){
      (ENV as any).JWT_PREV_SECRET = ENV.JWT_SECRET;
      (ENV as any).JWT_PREV_EXP_TS = Date.now() + Math.max(0, Number(graceSec))*1000;
      (ENV as any).JWT_SECRET = String(value);
      return { ok:true, name, graceSec };
    }
    if (name === "WEBHOOK_SECRET"){
      (ENV as any).WEBHOOK_SECRET = String(value);
      return { ok:true, name };
    }
    return res.status(400).send({error:"unsupported_secret"});
  });
}
