
import { ENV } from "../env.js";
import { request as httpReq } from "http";
import { request as httpsReq } from "https";
export function exportSpan(name:string, traceId:string, durationMs:number){
  if (!ENV.ZIPKIN_URL) return;
  const u = new URL(ENV.ZIPKIN_URL);
  const lib = u.protocol === "https:" ? httpsReq : httpReq;
  const payload = JSON.stringify([{
    traceId, id: traceId.slice(16), name, timestamp: Date.now()*1000, duration: durationMs*1000, localEndpoint:{ serviceName:"mobile-edge" }
  }]);
  const req = lib({ method:"POST", hostname:u.hostname, port:u.port, path:u.pathname+u.search, headers:{ "content-type":"application/json", "content-length": Buffer.byteLength(payload).toString() }}, res=>{ res.on("data",()=>{}); });
  req.on("error", ()=>{}); req.write(payload); req.end();
}
