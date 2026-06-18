import Fastify from "fastify";
import cors from "@fastify/cors";
import helmet from "@fastify/helmet";
import rateLimit from "@fastify/rate-limit";
import rawBody from "fastify-raw-body";
import { assertRuntimeEnv, ENV } from "./env.js";
import { traceMw } from "./middleware/trace.js";
import { M } from "./metrics.js";
import routeAdmin from "./routes/admin.js";
import routeApiKey from "./routes/apikey.js";
import routeAnalytic from "./routes/analytic.js";
import routeBackup from "./routes/backup.js";
import routeConfig from "./routes/config.js";
import routeCore from "./routes/core.js";
import routeEntitlement from "./routes/entitlement.js";
import routeFlag from "./routes/flag.js";
import routeHealth from "./routes/health.js";
import routeMetrics from "./routes/metrics.js";
import routePush from "./routes/push.js";
import routeReceipt from "./routes/receipt.js";
import routeSecret from "./routes/secret.js";
import routeSession from "./routes/session.js";
import routeSync from "./routes/sync.js";
import routeWebhook from "./routes/webhook.js";
import { startRequeue } from "./worker/requeue.js";

assertRuntimeEnv();

const app = Fastify({ logger: true });
await app.register(helmet, { contentSecurityPolicy: false });
await app.register(cors, {
  origin(origin, callback) {
    const allowed = ENV.ALLOW_ORIGINS.includes("*") || !origin || ENV.ALLOW_ORIGINS.includes(origin);
    callback(null, allowed);
  },
});
await app.register(rateLimit, {
  global: false,
  max: ENV.RATE_MOBILE_RPM,
  timeWindow: "1 minute",
  keyGenerator: (request) => String(request.headers["x-api-key"] || request.ip),
});
await app.register(rawBody, { field: "rawBody", global: false, encoding: false, runFirst: true });

app.addHook("onRequest", async (request, reply) => {
  (request as any).startedAt = Date.now();
  await traceMw(request, reply);
});
app.addHook("onSend", async (request, reply, payload) => {
  const duration = Date.now() - Number((request as any).startedAt || Date.now());
  await M.observe(String((request as any).routerPath || request.routeOptions?.url || request.url), reply.statusCode || 200, duration);
  return payload;
});
app.setErrorHandler((error: any, _request, reply) => {
  const status = Number(error.statusCode || 500);
  reply.status(status).send({ error: status >= 500 ? "internal_error" : error.message });
});

await routeHealth(app);
await routeSession(app);
await routeFlag(app);
await routeConfig(app);
await routeEntitlement(app);
await routePush(app);
await routeReceipt(app);
await routeAnalytic(app);
await routeSync(app);
await routeCore(app);
await routeApiKey(app);
await routeAdmin(app);
await routeBackup(app);
await routeWebhook(app);
await routeSecret(app);
await routeMetrics(app);

startRequeue();

try {
  await app.listen({ port: ENV.PORT, host: "0.0.0.0" });
  app.log.info({ port: ENV.PORT }, "mobile-edge listening");
} catch (error) {
  app.log.error(error);
  process.exit(1);
}
