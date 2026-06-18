
import { FastifyRequest } from "fastify";
import { ENV } from "../env.js";
import { hmacHex } from "../util/hmac.js";
export function verifyAnalyticSignature(req: FastifyRequest, body: string | Buffer) {
  const sig = req.headers["x-sr-analytic-signature"];
  if (!sig || typeof sig !== "string") {
    const e:any = new Error("missing signature"); (e as any).statusCode = 400; throw e;
  }
  const calc = hmacHex(ENV.ANALYTIC_SECRET, body);
  if (calc.toLowerCase() !== String(sig).toLowerCase()) {
    const e:any = new Error("bad signature"); (e as any).statusCode = 401; throw e;
  }
}
