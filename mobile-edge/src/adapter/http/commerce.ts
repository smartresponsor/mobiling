
import { ENV } from "../../env.js";
import { request as httpReq } from "http";
import { request as httpsReq } from "https";

export async function verifyReceiptViaCore(payload: any): Promise<{ status:number, body:any }>{ 
  if (!ENV.CORE_COMMERCE_URL) throw new Error("CORE_COMMERCE_URL missing");
  const u = new URL(ENV.CORE_COMMERCE_URL.replace(/\/$/, '') + "/receipt/verify");
  const lib = u.protocol === "https:" ? httpsReq : httpReq;
  const body = JSON.stringify(payload);
  return await new Promise(resolve => {
    const req = lib({
      method: "POST",
      hostname: u.hostname,
      port: u.port,
      path: u.pathname + u.search,
      headers: {
        "content-type": "application/json",
        "content-length": Buffer.byteLength(body).toString(),
        ...(ENV.CORE_AUTH ? { "authorization": ENV.CORE_AUTH } : {})
      }
    }, res => {
      const chunks: Buffer[] = [];
      res.on("data", c => chunks.push(c));
      res.on("end", () => {
        const txt = Buffer.concat(chunks).toString("utf8");
        let obj: any = txt;
        try { obj = JSON.parse(txt); } catch {}
        resolve({ status: res.statusCode || 502, body: obj });
      });
    });
    req.on("error", _ => resolve({ status: 502, body: { error: "core_unreachable" } }));
    req.write(body); req.end();
  });
}
