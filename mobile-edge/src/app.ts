
import Fastify from "fastify";
import helmet from "@fastify/helmet";
import cors from "@fastify/cors";
import rateLimit from "@fastify/rate-limit";
import { ENV } from "./env.js";
import { traceMw } from "./middleware/trace.js";
import routeHealth from "./routes/health.js";
import routeSession from "./routes/session.js";
import routeFlag from "./routes/flag.js";
import { M } from "./metrics.js";

const app = Fastify({ logger: { transport: { target: 'pino/file', options: { destination: 1 } } } });
await app.register(helmet, { contentSecurityPolicy: false });
await app.register(cors, { origin: (origin, cb)=> cb(null, true) });
await app.register(rateLimit, { global:false, max: ENV.RATE_MOBILE_RPM, timeWindow: '1 minute', keyGenerator: (req)=> String(req.headers['x-api-key']||req.ip) });

app.addHook("onRequest", async (req, res)=> traceMw(req,res));
app.addHook("onSend", async (req, res, payload)=>{
  const t0 = (req as any).t0 || Date.now();
  const ms = Date.now()-t0;
  try{ await M.observe(req.routerPath||req.url, res.statusCode||200, ms); }catch{}
  return payload;
});
app.addHook("onRequest", async (req)=>{ (req as any).t0 = Date.now(); });

await routeHealth(app);
await routeSession(app);
await routeFlag(app);
await M.route(app);

const start = async () => {
  try { await app.listen({ port: ENV.PORT, host: "0.0.0.0" }); app.log.info("listening on " + ENV.PORT); }
  catch (err) { app.log.error(err); process.exit(1); }
};
start();
