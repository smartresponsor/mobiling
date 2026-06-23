
import { FastifyInstance } from "fastify";
import { ENV } from "../env.js";
import { hmacHex, signEd25519 } from "../util/hmac.js";
const SNAP: Record<string, any> = {
  "1": { flag:{ wifi_only:true, paywall:false }, sample:{ view:0.2, ping:1.0 } },
  "2": { flag:{ wifi_only:false, paywall:true }, sample:{ view:0.3, ping:1.0 } }
};
function etagOf(s:string){ return '"'+hmacHex("etag", s).slice(0,16)+'"'; }
export default async function route(app: FastifyInstance){
  app.get("/config", async (req:any, res)=>{
    const v = (req.query?.v || ENV.CONFIG_VERSION).toString();
    const body = JSON.stringify(SNAP[v] || SNAP["1"]);
    const tag = etagOf(body);
    if (req.headers["if-none-match"] === tag){ return res.status(304).send(); }
    res.header("ETag", tag);
    res.header("X-SR-Config-Signature", "v=1;sha256="+hmacHex(ENV.CONFIG_SECRET_V1, body));
    if (ENV.CONFIG_ED25519_PRIVATE){
      const sig = signEd25519(ENV.CONFIG_ED25519_PRIVATE, Buffer.from(body));
      res.header("X-SR-Config-Signature-V2", "v=2;alg=ed25519;kid="+ENV.CONFIG_KEY_ID+";sig="+sig.toString("base64url"));
    }
    res.header("Cache-Control", "public, max-age=60");
    res.type("application/json").send(body);
  });
}
