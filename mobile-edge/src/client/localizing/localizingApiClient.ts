import { request as httpRequest } from "http";
import { request as httpsRequest } from "https";
import { ENV } from "../../env.js";

export interface LocalizingApiErrorPayload {
  code: string;
  message: string;
}

export interface LocalizingApiResponse {
  status: number;
  body: unknown;
}

const LOCALIZING_API_UNAVAILABLE_PAYLOAD: LocalizingApiErrorPayload = {
  code: "localizing_api_unavailable",
  message: "Localizing API is unavailable from mobile-edge.",
};

export class LocalizingApiClient {
  constructor(
    private readonly baseUrl: string = ENV.LOCALIZING_API_BASE_URL,
    private readonly timeoutMs: number = ENV.LOCALIZING_API_TIMEOUT_MS,
  ) {
  }

  listLocales(forwardedHeaders: Record<string, string> = {}): Promise<LocalizingApiResponse> {
    return this.request("GET", "/api/locale", forwardedHeaders);
  }

  resolveFallback(code: string, forwardedHeaders: Record<string, string> = {}): Promise<LocalizingApiResponse> {
    return this.request("GET", `/api/locale/${encodeURIComponent(code)}/fallback`, forwardedHeaders);
  }

  resolveMessage(locale: string, domain: string, key: string, forwardedHeaders: Record<string, string> = {}): Promise<LocalizingApiResponse> {
    const params = new URLSearchParams({ locale, domain, key });
    return this.request("GET", `/api/locale/translation/message/resolve?${params.toString()}`, forwardedHeaders);
  }

  private async request(method: string, path: string, forwardedHeaders: Record<string, string>): Promise<LocalizingApiResponse> {
    const baseUrl = this.baseUrl.trim();

    if ("" === baseUrl) {
      return this.unavailable();
    }

    let url: URL;

    try {
      url = new URL(baseUrl.replace(/\/$/, "") + path);
    } catch {
      return this.unavailable();
    }

    const transport = "https:" === url.protocol ? httpsRequest : httpRequest;

    return await new Promise<LocalizingApiResponse>((resolve) => {
      const req = transport(
        {
          method,
          hostname: url.hostname,
          port: url.port,
          path: `${url.pathname}${url.search}`,
          headers: {
            accept: "application/json",
            ...forwardedHeaders,
          },
          timeout: this.timeoutMs,
        },
        (response) => {
          const chunks: Buffer[] = [];

          response.on("data", (chunk: Buffer) => chunks.push(chunk));
          response.on("end", () => {
            const status = response.statusCode || 502;

            if (status >= 300 && status < 400) {
              resolve(this.unavailable());
              return;
            }

            const text = Buffer.concat(chunks).toString("utf8");
            resolve({
              status,
              body: this.parseResponseBody(text),
            });
          });
        },
      );

      req.on("timeout", () => {
        try {
          req.destroy();
        } catch {
        }

        resolve(this.unavailable());
      });
      req.on("error", () => resolve(this.unavailable()));
      req.end();
    });
  }

  private unavailable(): LocalizingApiResponse {
    return {
      status: 503,
      body: LOCALIZING_API_UNAVAILABLE_PAYLOAD,
    };
  }

  private parseResponseBody(text: string): unknown {
    if ("" === text.trim()) {
      return {};
    }

    try {
      return JSON.parse(text);
    } catch {
      return text;
    }
  }
}
