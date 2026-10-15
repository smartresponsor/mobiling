import { ENV } from "../../env.js";
import { resolveUpstreamBaseUrl } from "../../runtime/applicationRuntimeResolver.js";

export interface CurrencingApiResponse {
  status: number;
  body: unknown;
}

export class CurrencingApiClient {
  constructor(
    private readonly baseUrl: string = ENV.CURRENCING_API_BASE_URL,
    private readonly timeoutMs: number = ENV.CURRENCING_API_TIMEOUT_MS,
  ) {
  }

  async get(path: string, forwardedHeaders: Record<string, string> = {}): Promise<CurrencingApiResponse> {
    const baseUrl = await resolveUpstreamBaseUrl(forwardedHeaders, this.baseUrl);
    if ("" === baseUrl) {
      return this.unavailable();
    }

    let url: URL;
    try {
      url = new URL(baseUrl.replace(/\/$/, "") + path);
    } catch {
      return this.unavailable();
    }

    try {
      const response = await fetch(url, {
        method: "GET",
        headers: { accept: "application/json", ...forwardedHeaders },
        signal: AbortSignal.timeout(this.timeoutMs),
        redirect: "manual",
      });

      if (response.status >= 300 && response.status < 400) {
        return this.unavailable();
      }

      const text = await response.text();
      let body: unknown = {};
      if ("" !== text.trim()) {
        try {
          body = JSON.parse(text);
        } catch {
          body = text;
        }
      }

      return { status: response.status, body };
    } catch {
      return this.unavailable();
    }
  }

  private unavailable(): CurrencingApiResponse {
    return { status: 503, body: { code: "currencing_api_unavailable", message: "Currencing API is unavailable from mobile-edge." } };
  }
}
