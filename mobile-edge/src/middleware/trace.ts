
import { randomUUID } from "crypto";
import { ENV } from "../env.js";
export async function traceMw(req:any, res:any){
  const id = (req.headers["x-trace-id"] as string) || randomUUID();
  (req as any).traceId = id;
  res.header(ENV.TRACE_RESPONSE_HEADER, id);
}
