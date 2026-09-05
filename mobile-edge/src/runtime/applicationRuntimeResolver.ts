import { request as httpRequest } from "http";
import { request as httpsRequest } from "https";
import { ENV } from "../env.js";

export interface EffectiveApplicationRuntime {
  applicationKey: string;
  environment: string;
  runtimeMode: "host_shared" | "custom_domain";
  bootstrapOrigin: string;
  effectiveOrigin: string;
  fallbackOrigin: string;
}

export class ApplicationRuntimeResolver {
  private readonly cache = new Map<string, { expiresAt: number; runtime: EffectiveApplicationRuntime }>();

  constructor(
    private readonly baseUrl: string = ENV.APPLICATION_RUNTIME_API_BASE_URL,
    private readonly timeoutMs: number = ENV.APPLICATION_RUNTIME_API_TIMEOUT_MS,
    private readonly ttlMs: number = 30000,
  ) {}

  async resolve(applicationKey: string, environment: string): Promise<EffectiveApplicationRuntime | null> {
    const app = applicationKey.trim();
    const env = environment.trim();
    if (!app || !env) return null;

    const key = `${app}:${env}`;
    const cached = this.cache.get(key);
    if (cached && cached.expiresAt > Date.now()) return cached.runtime;

    const runtime = await this.fetchRuntime(app, env);
    if (runtime) this.cache.set(key, { expiresAt: Date.now() + this.ttlMs, runtime });
    return runtime;
  }

  private async fetchRuntime(applicationKey: string, environment: string): Promise<EffectiveApplicationRuntime | null> {
    let url: URL;
    try {
      url = new URL(this.baseUrl.replace(/\/$/, "") + `/api/application/runtime/${encodeURIComponent(applicationKey)}/${encodeURIComponent(environment)}`);
    } catch {
      return null;
    }

    const transport = "https:" === url.protocol ? httpsRequest : httpRequest;
    return await new Promise((resolve) => {
      const req = transport({
        method: "GET",
        hostname: url.hostname,
        port: url.port,
        path: `${url.pathname}${url.search}`,
        headers: { accept: "application/json" },
        timeout: this.timeoutMs,
      }, (response) => {
        const chunks: Buffer[] = [];
        response.on("data", (chunk: Buffer) => chunks.push(chunk));
        response.on("end", () => {
          if ((response.statusCode || 500) < 200 || (response.statusCode || 500) >= 300) return resolve(null);
          try {
            const body = JSON.parse(Buffer.concat(chunks).toString("utf8")) as Partial<EffectiveApplicationRuntime>;
            if (
              "string" === typeof body.applicationKey
              && "string" === typeof body.environment
              && ("host_shared" === body.runtimeMode || "custom_domain" === body.runtimeMode)
              && "string" === typeof body.bootstrapOrigin
              && "string" === typeof body.effectiveOrigin
              && "string" === typeof body.fallbackOrigin
            ) return resolve(body as EffectiveApplicationRuntime);
          } catch {}
          resolve(null);
        });
      });
      req.on("timeout", () => { req.destroy(); resolve(null); });
      req.on("error", () => resolve(null));
      req.end();
    });
  }
}

const sharedApplicationRuntimeResolver = new ApplicationRuntimeResolver();

export async function resolveUpstreamBaseUrl(
  forwardedHeaders: Record<string, string>,
  fallbackBaseUrl: string,
): Promise<string> {
  const runtime = await sharedApplicationRuntimeResolver.resolve(
    forwardedHeaders["x-application-key"] || "",
    forwardedHeaders["x-application-environment"] || "",
  );

  return (runtime?.effectiveOrigin || fallbackBaseUrl).trim();
}
