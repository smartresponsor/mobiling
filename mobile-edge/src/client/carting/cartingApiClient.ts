import { request as httpRequest } from "http";
import { request as httpsRequest } from "https";
import { ENV } from "../../env.js";
import { resolveUpstreamBaseUrl } from "../../runtime/applicationRuntimeResolver.js";

export interface CartingApiErrorPayload {
  code: string;
  message: string;
  fieldErrors?: Record<string, string[]> | null;
}

export interface CartingApiResponse {
  status: number;
  body: unknown;
  cartToken?: string;
}

const CARTING_API_UNAVAILABLE_PAYLOAD: CartingApiErrorPayload = {
  code: "carting_api_unavailable",
  message: "Carting API is unavailable from mobile-edge.",
};

export class CartingApiClient {
  constructor(
    private readonly baseUrl: string = ENV.CARTING_API_BASE_URL,
    private readonly timeoutMs: number = ENV.CARTING_API_TIMEOUT_MS,
  ) {
  }

  getCurrentCart(forwardedHeaders: Record<string, string> = {}): Promise<CartingApiResponse> {
    return this.request("GET", "/api/cart/current", null, forwardedHeaders);
  }

  addItem(body: unknown, forwardedHeaders: Record<string, string> = {}): Promise<CartingApiResponse> {
    return this.request("POST", "/api/cart/item", body, forwardedHeaders);
  }

  prepareCheckoutHandoff(forwardedHeaders: Record<string, string> = {}): Promise<CartingApiResponse> {
    return this.request("POST", "/api/cart/checkout-handoff", null, forwardedHeaders);
  }

  private async request(method: string, path: string, body: unknown, forwardedHeaders: Record<string, string>): Promise<CartingApiResponse> {
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

    const payload = null === body ? "" : JSON.stringify(body);
    const transport = "https:" === url.protocol ? httpsRequest : httpRequest;

    return await new Promise<CartingApiResponse>((resolve) => {
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
            const status = response.statusCode || 502;

            if (status >= 300 && status < 400) {
              resolve(this.unavailable());
              return;
            }

            const text = Buffer.concat(chunks).toString("utf8");
            const cartTokenHeader = response.headers["x-cart-token"];
            resolve({
              status,
              body: this.parseResponseBody(text),
              ...(typeof cartTokenHeader === "string" && cartTokenHeader.trim() !== "" ? { cartToken: cartTokenHeader.trim() } : {}),
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

  private unavailable(): CartingApiResponse {
    return {
      status: 503,
      body: CARTING_API_UNAVAILABLE_PAYLOAD,
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
