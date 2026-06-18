
import { FastifyRequest } from "fastify";
import { verifyJwt } from "../auth/jwt.js";
export async function requireJwt(req: FastifyRequest){
  const h = req.headers["authorization"];
  if (!h || !h.toLowerCase().startsWith("bearer ")){
    const e:any = new Error("unauthorized"); (e as any).statusCode = 401; throw e;
  }
  const token = h.slice(7);
  try { (req as any).jwt = await verifyJwt(token); }
  catch { const e:any = new Error("unauthorized"); (e as any).statusCode = 401; throw e; }
}
