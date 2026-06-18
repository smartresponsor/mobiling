import type { FastifyInstance } from "fastify";
import { ENV } from "./env.js";

let client: any;
let register: any;
let requestCounter: any;
let durationHistogram: any;
let circuitCounter: any;
let eventCounter: any;

async function ensure(): Promise<boolean> {
  if (!ENV.METRICS_ENABLED) return false;
  if (!client) {
    client = await import("prom-client");
    register = client.register;
    client.collectDefaultMetrics();
    requestCounter = new client.Counter({ name: "http_requests_total", help: "HTTP request count", labelNames: ["route", "code"] });
    durationHistogram = new client.Histogram({ name: "http_duration_ms", help: "HTTP handler duration", buckets: [25, 50, 100, 250, 500, 1000, 2000] });
    circuitCounter = new client.Counter({ name: "circuit_state_total", help: "Circuit state transitions", labelNames: ["state"] });
    eventCounter = new client.Counter({ name: "mobile_edge_event_total", help: "Mobile edge event count", labelNames: ["name", "label"] });
  }
  return true;
}

export const M = {
  async route(app: FastifyInstance): Promise<void> {
    if (!(await ensure())) return;
    app.get("/metrics", async (_request, reply) => {
      reply.header("Content-Type", register.contentType);
      return register.metrics();
    });
  },
  async observe(route: string, code: number, durationMs: number): Promise<void> {
    if (!(await ensure())) return;
    requestCounter.inc({ route, code: String(code) });
    durationHistogram.observe(durationMs);
  },
  async circuit(state: "open" | "half_open" | "closed"): Promise<void> {
    if (!(await ensure())) return;
    circuitCounter.inc({ state });
  },
  async inc(name: string, label: Record<string, unknown> = {}): Promise<void> {
    if (!(await ensure())) return;
    const serialized = Object.entries(label).sort(([left], [right]) => left.localeCompare(right)).map(([key, value]) => `${key}=${String(value)}`).join(",");
    eventCounter.inc({ name, label: serialized });
  },
};
