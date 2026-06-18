
import { FastifyInstance } from "fastify";
import { evalFlag, Snapshot } from "../service/flag.js";
const SNAP: Snapshot = { flags: {
  "paywall": { key:"paywall", pct: 0.3, app:"android" },
  "wifi_only": { key:"wifi_only" }
}};
export default async function route(app: FastifyInstance){
  app.post("/mobile/flag/eval/:key", async (req:any, res)=>{
    const key = req.params.key; const ctx = req.body || {};
    const on = evalFlag(SNAP, key, ctx);
    return { key, on };
  });
}
