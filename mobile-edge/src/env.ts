const integer = (name: string, fallback: number): number =>
  Number.parseInt(process.env[name] || String(fallback), 10);

const boolean = (name: string, fallback: boolean): boolean =>
  (process.env[name] || String(fallback)).toLowerCase() === "true";

const list = (name: string, fallback = ""): string[] =>
  (process.env[name] || fallback).split(",").map((value) => value.trim()).filter(Boolean);

export const ENV = {
  NODE_ENV: process.env.NODE_ENV || "development",
  PORT: integer("PORT", 8080),
  ADMIN_TOKEN: process.env.ADMIN_TOKEN || "dev",
  ADMIN_ALLOW_IPS: list("ADMIN_ALLOW_IPS"),
  CSRF_REQUIRED: boolean("CSRF_REQUIRED", false),
  JWT_SECRET: process.env.JWT_SECRET || "devsecret",
  JWT_PREV_SECRET: process.env.JWT_PREV_SECRET || "",
  JWT_PREV_EXP_TS: integer("JWT_PREV_EXP_TS", 0),
  JWT_TTL_SEC: integer("JWT_TTL_SEC", 3600),
  DEV_BEARER: process.env.DEV_BEARER || "Bearer dev",
  METRICS_ENABLED: boolean("METRICS_ENABLED", false),
  ALLOW_ORIGINS: list("ALLOW_ORIGINS", "*"),
  TRACE_RESPONSE_HEADER: process.env.TRACE_RESPONSE_HEADER || "Trace-Id",
  ZIPKIN_URL: process.env.ZIPKIN_URL || "",
  LOG_SHIP_URL: process.env.LOG_SHIP_URL || "",
  CONFIG_VERSION: process.env.CONFIG_VERSION || "1",
  CONFIG_SECRET_V1: process.env.CONFIG_SECRET_V1 || "changeme",
  CONFIG_ED25519_PRIVATE: process.env.CONFIG_ED25519_PRIVATE || "",
  CONFIG_KEY_ID: process.env.CONFIG_KEY_ID || "k1",
  ANALYTIC_SECRET: process.env.ANALYTIC_SECRET || "dev-analytic",
  RATE_MOBILE_RPM: integer("RATE_MOBILE_RPM", 120),
  CORE_COMMERCE_URL: process.env.CORE_COMMERCE_URL || "http://localhost:9000",
  CORE_AUTH: process.env.CORE_AUTH || "",
  CORE_TIMEOUT_MS: integer("CORE_TIMEOUT_MS", 3000),
  CB_FAILS_OPEN: integer("CB_FAILS_OPEN", 5),
  CB_COOLDOWN_MS: integer("CB_COOLDOWN_MS", 20000),
  STORE: (process.env.STORE || "sqlite").toLowerCase(),
  DB_PATH: process.env.DB_PATH || "data/mobile-edge.sqlite",
  DB_URL: process.env.DB_URL || "postgres://postgres:postgres@localhost:5432/mobile_edge",
  WEBHOOK_URL_REQUEUE: process.env.WEBHOOK_URL_REQUEUE || "",
  WEBHOOK_URL_PURGE: process.env.WEBHOOK_URL_PURGE || "",
  WEBHOOK_SECRET: process.env.WEBHOOK_SECRET || "dev-webhook",
  S3_BUCKET: process.env.S3_BUCKET || "",
  S3_REGION: process.env.S3_REGION || "",
  REQUEUE_ENABLED: boolean("REQUEUE_ENABLED", false),
  REQUEUE_BASE_MS: integer("REQUEUE_BASE_MS", 1000),
  REQUEUE_CAP_MS: integer("REQUEUE_CAP_MS", 600000),
  REQUEUE_RPM: integer("REQUEUE_RPM", 60),
  REQUEUE_DAILY: integer("REQUEUE_DAILY", 10000),
  REQUEUE_CONCURRENCY: integer("REQUEUE_CONCURRENCY", 4),
  DLQ_TTL_DAYS: integer("DLQ_TTL_DAYS", 30),
};

export function assertRuntimeEnv(): void {
  if (ENV.NODE_ENV !== "production") return;
  const invalid = [
    ["ADMIN_TOKEN", ENV.ADMIN_TOKEN, "dev"],
    ["JWT_SECRET", ENV.JWT_SECRET, "devsecret"],
    ["CONFIG_SECRET_V1", ENV.CONFIG_SECRET_V1, "changeme"],
    ["ANALYTIC_SECRET", ENV.ANALYTIC_SECRET, "dev-analytic"],
  ].filter(([, value, forbidden]) => value === forbidden).map(([name]) => name);
  if (invalid.length > 0) {
    throw new Error(`Unsafe production configuration: ${invalid.join(", ")}`);
  }
}
