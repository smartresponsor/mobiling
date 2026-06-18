
import { FastifyInstance } from "fastify";
import { getStore } from "../repository/index.js";

function adminGuard(req:any){ if(req.headers["x-admin-token"] !== (process.env.ADMIN_TOKEN||"")){ const e:any=new Error("forbidden"); (e as any).statusCode=403; throw e; } }

export default async function route(app: FastifyInstance){
  app.get("/admin/webhook/subscription", async (req:any)=>{ adminGuard(req); const s=await getStore(); return await s.whList(); });
  app.post("/admin/webhook/subscription", async (req:any,res)=>{
    adminGuard(req);
    const { id, url, secret, enabled=true, events=[], retry_max=12, backoff_base_ms=1000, backoff_cap_ms=600000 } = req.body || {};
    if(!id||!url||!secret||!Array.isArray(events)||events.length===0) return res.status(400).send({error:"bad_request"});
    const s=await getStore(); await s.whUpsert({ id:String(id), url:String(url), secret:String(secret), enabled:!!enabled, events:events.map(String), retry_max:Number(retry_max), backoff_base_ms:Number(backoff_base_ms), backoff_cap_ms:Number(backoff_cap_ms) });
    return { ok:true };
  });
  app.get("/admin/webhook/delivery", async (req:any)=>{
    adminGuard(req);
    const s=await getStore(); const q=req.query||{};
    return await s.whSearch(q.event, q.status, q.sinceTs?Number(q.sinceTs):undefined, Math.min(1000, Number(q.limit||200)));
  });
  app.post("/admin/webhook/delivery/replay/:id", async (req:any,res)=>{
    adminGuard(req);
    const s=await getStore(); const d=await s.whGet(Number(req.params.id)); if(!d) return res.status(404).send({error:"not_found"});
    await s.whUpdateAttempt(d.id, d.attempts, 0, d.lastStatus??null, d.durationMs??null); return { ok:true, id:d.id };
  });
}
