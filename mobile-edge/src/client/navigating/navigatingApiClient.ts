import { request as httpRequest } from "http";
import { request as httpsRequest } from "https";
import { ENV } from "../../env.js";

export interface NavigatingApiErrorPayload {
  code: string;
  message: string;
}

export interface NavigatingApiResponse {
  status: number;
  body: unknown;
}

const NAVIGATING_API_UNAVAILABLE_PAYLOAD: NavigatingApiErrorPayload = {
  code: "navigating_api_unavailable",
  message: "Navigating API is unavailable from mobile-edge.",
};

export class NavigatingApiClient {
  constructor(
    private readonly baseUrl: string = ENV.NAVIGATING_API_BASE_URL,
    private readonly timeoutMs: number = ENV.NAVIGATING_API_TIMEOUT_MS,
  ) {
  }

  getMobileShell(forwardedHeaders: Record<string, string> = {}): Promise<NavigatingApiResponse> {
    return this.request("GET", "/api/navigation/mobile/shell", forwardedHeaders);
  }

  private async request(method: string, path: string, forwardedHeaders: Record<string, string>): Promise<NavigatingApiResponse> {
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

    return await new Promise<NavigatingApiResponse>((resolve) => {
      const request = transport(
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

      request.on("timeout", () => {
        try {
          request.destroy();
        } catch {
        }

        resolve(this.unavailable());
      });
      request.on("error", () => resolve(this.unavailable()));
      request.end();
    });
  }

  private unavailable(): NavigatingApiResponse {
    return {
      status: 503,
      body: NAVIGATING_API_UNAVAILABLE_PAYLOAD,
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
