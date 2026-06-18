
export const ENV = {
  PORT: parseInt(process.env.PORT || "8080", 10),
  ADMIN_TOKEN: process.env.ADMIN_TOKEN || "dev",
  JWT_SECRET: process.env.JWT_SECRET || "devsecret",
  JWT_PREV_SECRET: process.env.JWT_PREV_SECRET || "", // для grace-окна
  JWT_PREV_EXP_TS: parseInt(process.env.JWT_PREV_EXP_TS || "0", 10),
  JWT_TTL_SEC: parseInt(process.env.JWT_TTL_SEC || "3600", 10),
  METRICS_ENABLED: (process.env.METRICS_ENABLED || "false").toLowerCase() === "true",
  ALLOW_ORIGINS: (process.env.ALLOW_ORIGINS || "*").split(",").map(s=>s.trim()).filter(Boolean),
  TRACE_RESPONSE_HEADER: (process.env.TRACE_RESPONSE_HEADER || "Trace-Id"),
  ZIPKIN_URL: process.env.ZIPKIN_URL || "",
  CONFIG_VERSION: process.env.CONFIG_VERSION || "1",
  CONFIG_SECRET_V1: process.env.CONFIG_SECRET_V1 || "changeme",
  CONFIG_ED25519_PRIVATE: process.env.CONFIG_ED25519_PRIVATE || "",
  CONFIG_KEY_ID: process.env.CONFIG_KEY_ID || "k1",
  RATE_MOBILE_RPM: parseInt(process.env.RATE_MOBILE_RPM || "120", 10),
  CORE_COMMERCE_URL: process.env.CORE_COMMERCE_URL || "http://localhost:9000",
  CORE_TIMEOUT_MS: parseInt(process.env.CORE_TIMEOUT_MS || "3000", 10),
  CB_FAILS_OPEN: parseInt(process.env.CB_FAILS_OPEN || "5", 10),
  CB_COOLDOWN_MS: parseInt(process.env.CB_COOLDOWN_MS || "20000", 10)
};
