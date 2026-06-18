
import { getStore } from "../repository/index.js";
import { signHeaderV2 } from "../util/hmac.js";
import { request as httpReq } from "http";
import { request as httpsReq } from "https";
import { M } from "../metrics.js";

function jitter(ms:number){ return Math.floor(ms * (0.5 + Math.random())); }
function exp(base:number, cap:number, attempt:number){ let d = base * Math.pow(2, attempt); if (d > cap) d = cap; return jitter(d); }

async function deliver(url:string, secret:string, body:string){
  const u = new URL(url); const lib = u.protocol === "https:" ? httpsReq : httpReq;
  const ts = Math.floor(Date.now()/1000);
  const sig = signHeaderV2(secret, body, ts);
  const t0 = Date.now();
  return await new Promise<{status:number, ms:number}>(resolve => {
    const req = lib({ method:"POST", hostname:u.hostname, port:u.port, path:u.pathname+u.search, headers:{
      "content-type":"application/json","content-length":Buffer.byteLength(body).toString(),"x-sr-webhook-signature":sig
    }}, res => { res.on("data",()=>{}); res.on("end",()=> resolve({ status: res.statusCode || 0, ms: Date.now()-t0 })); });
    req.on("error",()=> resolve({ status: 0, ms: Date.now()-t0 })); req.write(body); req.end();
  });
}

export function startWebhookWorker(){
  setInterval(async ()=>{
    const s = await getStore();
    const due = await s.whDue(Date.now(), 50);
    for (const d of due){
      const subs = await s.whList();
      const sub = subs.find(x => x.id === d.subId); if (!sub || !sub.enabled){ await M.inc("whTotal", {status:"failed"}); continue; }
      const body = JSON.stringify({ type:d.event, ts:d.ts, deliveryId:d.id, payload:d.payload });
      const out = await deliver(sub.url, sub.secret, body);
      await M.observeDur(out.ms);
      if (out.status >= 200 && out.status < 300){
        await s.whMarkSent(d.id, out.status, out.ms);
        await M.inc("whTotal", {status:"sent"});
      } else {
        const next = Date.now() + exp(sub.backoff_base_ms, sub.backoff_cap_ms, d.attempts);
        await s.whUpdateAttempt(d.id, d.attempts+1, next, out.status || null, out.ms);
        await M.inc("whRetry");
      }
    }
  }, 1000).unref();
}
