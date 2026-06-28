const publicOrigin = (process.env.MOBILE_EDGE_PUBLIC_ORIGIN || process.env.MOBILE_EDGE_SMOKE_PUBLIC_ORIGIN || "").trim();

if ("" === publicOrigin) {
  console.error("Mobile public smoke requires MOBILE_EDGE_PUBLIC_ORIGIN or MOBILE_EDGE_SMOKE_PUBLIC_ORIGIN.");
  process.exitCode = 1;
} else {
  process.env.MOBILE_EDGE_SMOKE_ORIGIN = publicOrigin;

  await import("./mobile-live-smoke-check.mjs");
}
