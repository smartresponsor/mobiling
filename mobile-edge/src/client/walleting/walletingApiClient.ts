import { request as httpRequest } from "http";
import { request as httpsRequest } from "https";
import { ENV } from "../../env.js";

export interface WalletingApiResponse { status: number; body: unknown }

export class WalletingApiClient {
  constructor(private readonly baseUrl = ENV.CRUDING_API_BASE_URL, private readonly timeoutMs = ENV.CRUDING_API_TIMEOUT_MS) {}

  balance(headers: Record<string, string>): Promise<WalletingApiResponse> {
    return this.get("/api/wallet/balance", headers);
  }

  transaction(headers: Record<string, string>): Promise<WalletingApiResponse> {
    return this.get("/api/wallet/transaction", headers);
  }

  funding(headers: Record<string, string>): Promise<WalletingApiResponse> {
    return this.get("/api/wallet/funding", headers);
  }

  withdrawal(headers: Record<string, string>): Promise<WalletingApiResponse> {
    return this.get("/api/wallet/withdrawal", headers);
  }

  withdrawalShow(headers: Record<string, string>, id: string): Promise<WalletingApiResponse> {
    return this.get(`/api/wallet/withdrawal/${encodeURIComponent(id)}`, headers);
  }

  withdrawalDestination(headers: Record<string, string>): Promise<WalletingApiResponse> {
    return this.get("/api/wallet/withdrawal/destination", headers);
  }

  withdrawalRequest(headers: Record<string, string>, body: unknown): Promise<WalletingApiResponse> {
    return this.send("POST", "/api/wallet/withdrawal/request", headers, body);
  }

  withdrawalCancel(headers: Record<string, string>, id: string): Promise<WalletingApiResponse> {
    return this.send("POST", `/api/wallet/withdrawal/cancel/${encodeURIComponent(id)}`, headers, {});
  }

  private get(path: string, headers: Record<string, string>): Promise<WalletingApiResponse> {
    return this.send("GET", path, headers);
  }

  private send(method: "GET" | "POST", path: string, headers: Record<string, string>, body?: unknown): Promise<WalletingApiResponse> {
    const baseUrl = this.baseUrl.trim();
    if (!baseUrl) return Promise.resolve({ status: 503, body: { code: "walleting_api_unavailable", message: "Walleting API is unavailable." } });
    let url: URL;
    try { url = new URL(baseUrl.replace(/\/$/, "") + path); }
    catch { return Promise.resolve({ status: 503, body: { code: "walleting_api_unavailable", message: "Walleting API URL is invalid." } }); }
    const transport = url.protocol === "https:" ? httpsRequest : httpRequest;
    const payload = method === "POST" ? JSON.stringify(body ?? {}) : null;
    return new Promise((resolve) => {
      const req = transport({
        method,
        hostname: url.hostname,
        port: url.port,
        path: url.pathname,
        headers: {
          accept: "application/json",
          ...(payload !== null ? { "content-type": "application/json", "content-length": Buffer.byteLength(payload).toString() } : {}),
          ...headers,
        },
        timeout: this.timeoutMs,
      }, (response) => {
        const chunks: Buffer[] = [];
        response.on("data", (chunk: Buffer) => chunks.push(chunk));
        response.on("end", () => {
          const text = Buffer.concat(chunks).toString("utf8");
          let body: unknown = {};
          if (text.trim()) try { body = JSON.parse(text); } catch { body = { message: text }; }
          resolve({ status: response.statusCode || 502, body });
        });
      });
      req.on("timeout", () => { req.destroy(); resolve({ status: 503, body: { code: "walleting_api_timeout", message: "Walleting API request timed out." } }); });
      req.on("error", () => resolve({ status: 503, body: { code: "walleting_api_unavailable", message: "Walleting API request failed." } }));
      if (payload !== null) req.write(payload);
      req.end();
    });
  }
}
