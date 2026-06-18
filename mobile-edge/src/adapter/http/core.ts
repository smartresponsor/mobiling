
import { request as httpReq } from "http";
import { request as httpsReq } from "https";
import { ENV } from "../../env.js";
import * as CB from "../../service/circuit.js";
function doReq(path:string, body:any): Promise<{status:number, body:any}>{
  return new Promise(resolve=>{
    const u = new URL(ENV.CORE_COMMERCE_URL);
    const lib = u.protocol === "https:" ? httpsReq : httpReq;
    const payload = JSON.stringify(body||{});
    const req = lib({ method:"POST", hostname:u.hostname, port:u.port, path: (u.pathname.replace(/\/$/,'') + path), headers:{
      "content-type":"application/json", "content-length": Buffer.byteLength(payload).toString()
    }, timeout: ENV.CORE_TIMEOUT_MS }, res=>{
      const chunks:any[]=[]; res.on("data", c=>chunks.push(c)); res.on("end", ()=>{
        const txt = Buffer.concat(chunks).toString("utf8");
        try{ resolve({ status: res.statusCode||0, body: JSON.parse(txt||"{}")}); }catch{ resolve({ status: res.statusCode||0, body: txt}); }
      });
    });
    req.on("timeout", ()=>{ try{req.destroy();}catch{}; resolve({status: 599, body:{}}); });
    req.on("error", ()=> resolve({status: 0, body:{}}));
    req.write(payload); req.end();
  });
}
export async function coreCall(path:string, body:any){
  if (!CB.allow()) return { status: 503, body: { error:"circuit_open" } };
  const out = await doReq(path, body);
  if (out.status >= 200 && out.status < 300){ CB.onSuccess(); } else { CB.onFail(); }
  return out;
}
