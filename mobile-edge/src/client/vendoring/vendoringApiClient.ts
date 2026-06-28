import { request as httpRequest } from "http";
import { request as httpsRequest } from "https";
import { ENV } from "../../env.js";

export interface VendoringApiErrorPayload {
  code: string;
  message: string;
  fieldErrors?: Record<string, string[]> | null;
}

export interface VendoringApiResponse {
  status: number;
  body: unknown;
}

const VENDORING_API_UNAVAILABLE_PAYLOAD: VendoringApiErrorPayload = {
  code: "vendoring_api_unavailable",
  message: "Vendoring API is unavailable from mobile-edge.",
};

export class VendoringApiClient {
  constructor(
    private readonly baseUrl: string = ENV.VENDORING_API_BASE_URL,
    private readonly timeoutMs: number = ENV.VENDORING_API_TIMEOUT_MS,
  ) {
  }

  getProfile(vendorId: string, forwardedHeaders: Record<string, string> = {}): Promise<VendoringApiResponse> {
    return this.request("GET", `/api/vendor/profile/show/${encodeURIComponent(vendorId)}`, null, forwardedHeaders);
  }

  getSummary(vendorId: string, forwardedHeaders: Record<string, string> = {}): Promise<VendoringApiResponse> {
    return this.request("GET", `/api/vendor/summary/show/${encodeURIComponent(vendorId)}`, null, forwardedHeaders);
  }

  getStatement(vendorId: string, forwardedHeaders: Record<string, string> = {}): Promise<VendoringApiResponse> {
    return this.request("GET", `/api/vendor/statement/show/${encodeURIComponent(vendorId)}`, null, forwardedHeaders);
  }

  private async request(method: string, path: string, body: unknown, forwardedHeaders: Record<string, string>): Promise<VendoringApiResponse> {
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

    const payload = null === body ? "" : JSON.stringify(body);
    const transport = "https:" === url.protocol ? httpsRequest : httpRequest;

    return await new Promise<VendoringApiResponse>((resolve) => {
      const request = transport(
        {
          method,
          hostname: url.hostname,
          port: url.port,
          path: `${url.pathname}${url.search}`,
          headers: {
            accept: "application/json",
            ...(payload ? { "content-type": "application/json", "content-length": Buffer.byteLength(payload).toString() } : {}),
            ...forwardedHeaders,
          },
          timeout: this.timeoutMs,
        },
        (response) => {
          const chunks: Buffer[] = [];

          response.on("data", (chunk: Buffer) => chunks.push(chunk));
          response.on("end", () => {
            const text = Buffer.concat(chunks).toString("utf8");
            resolve({
              status: response.statusCode || 502,
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

      if ("" !== payload) {
        request.write(payload);
      }

      request.end();
    });
  }

  private unavailable(): VendoringApiResponse {
    return {
      status: 503,
      body: VENDORING_API_UNAVAILABLE_PAYLOAD,
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
