
import { ENV } from "./env.js";
import type { FastifyInstance } from "fastify";
let client:any, register:any; let counters:any={}, hist:any={};
async function ensure(){
  if(!ENV.METRICS_ENABLED) return false;
  if(!client){
    const mod = await import("prom-client");
    client = mod; register = client.register; client.collectDefaultMetrics();
    counters.req = new client.Counter({ name:"http_requests_total", help:"Requests", labelNames:["route","code"] });
    hist.lat = new client.Histogram({ name:"http_duration_ms", help:"Handler time (ms)", buckets:[25,50,100,250,500,1000,2000] });
    counters.cb = new client.Counter({ name:"circuit_state_total", help:"Circuit state changes", labelNames:["state"] });
  }
  return true;
}
export const M = {
  async route(app: FastifyInstance){
    if(!ENV.METRICS_ENABLED) return; await ensure();
    app.get("/metrics", async (_req, res)=>{ res.header("Content-Type", register.contentType); return await register.metrics(); });
  },
  async observe(route:string, code:number, ms:number){
    if(!ENV.METRICS_ENABLED) return; await ensure(); counters.req.inc({route, code:String(code)}); hist.lat.observe(ms);
  },
  async circuit(state:"open"|"half_open"|"closed"){
    if(!ENV.METRICS_ENABLED) return; await ensure(); counters.cb.inc({state});
  }
};
