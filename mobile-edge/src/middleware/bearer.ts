
import { FastifyRequest } from "fastify";
import { ENV } from "../env.js";
export function requireBearer(req: FastifyRequest) {
  const h = req.headers["authorization"];
  if (!h || h !== ENV.DEV_BEARER) {
    const e:any = new Error("unauthorized");
    (e as any).statusCode = 401;
    throw e;
  }
}
