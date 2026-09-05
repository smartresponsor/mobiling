import { request as httpRequest } from "http";
import { request as httpsRequest } from "https";
import { ENV } from "../../env.js";
import { resolveUpstreamBaseUrl } from "../../runtime/applicationRuntimeResolver.js";

export interface CatalogingApiErrorPayload {
  code: string;
  message: string;
  fieldErrors?: Record<string, string[]> | null;
}

export interface CatalogingApiResponse {
  status: number;
  body: unknown;
}

const CATALOGING_API_UNAVAILABLE_PAYLOAD: CatalogingApiErrorPayload = {
  code: "cataloging_api_unavailable",
  message: "Cataloging API is unavailable from mobile-edge.",
};

export class CatalogingApiClient {
  constructor(
    private readonly baseUrl: string = ENV.CATALOGING_API_BASE_URL,
    private readonly timeoutMs: number = ENV.CATALOGING_API_TIMEOUT_MS,
  ) {
  }

  get(path: string, forwardedHeaders: Record<string, string> = {}): Promise<CatalogingApiResponse> {
    return this.request("GET", path, undefined, forwardedHeaders);
  }

  post(path: string, body: unknown, forwardedHeaders: Record<string, string> = {}): Promise<CatalogingApiResponse> {
    return this.request("POST", path, body, forwardedHeaders);
  }

  delete(path: string, forwardedHeaders: Record<string, string> = {}): Promise<CatalogingApiResponse> {
    return this.request("DELETE", path, undefined, forwardedHeaders);
  }

  private async request(method: string, path: string, body: unknown, forwardedHeaders: Record<string, string>): Promise<CatalogingApiResponse> {
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

    const transport = "https:" === url.protocol ? httpsRequest : httpRequest;

    return await new Promise<CatalogingApiResponse>((resolve) => {
      const request = transport(
        {
          method,
          hostname: url.hostname,
          port: url.port,
          path: `${url.pathname}${url.search}`,
          headers: {
            accept: "application/json",
            ...(undefined === body ? {} : { "content-type": "application/json" }),
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
            resolve({ status, body: this.parseResponseBody(text) });
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
      if (undefined !== body) {
        request.write(JSON.stringify(body));
      }
      request.end();
    });
  }

  private unavailable(): CatalogingApiResponse {
    return { status: 503, body: CATALOGING_API_UNAVAILABLE_PAYLOAD };
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
