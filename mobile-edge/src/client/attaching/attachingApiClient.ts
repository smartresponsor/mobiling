import { request as httpRequest } from "http";
import { request as httpsRequest } from "https";
import { ENV } from "../../env.js";

export interface AttachingApiErrorPayload {
  code: string;
  message: string;
  fieldErrors?: Record<string, string[]> | null;
}

export interface AttachingApiResponse {
  status: number;
  body: unknown;
}

export interface AttachingListQuery {
  ownerType?: string | null;
  ownerId?: string | null;
  context?: string | null;
  slot?: string | null;
}

const ATTACHING_API_UNAVAILABLE_PAYLOAD: AttachingApiErrorPayload = {
  code: "attaching_api_unavailable",
  message: "Attaching API is unavailable from mobile-edge.",
};

export class AttachingApiClient {
  constructor(
    private readonly baseUrl: string = ENV.ATTACHING_API_BASE_URL,
    private readonly timeoutMs: number = ENV.ATTACHING_API_TIMEOUT_MS,
  ) {
  }

  listAttachment(query: AttachingListQuery, forwardedHeaders: Record<string, string> = {}): Promise<AttachingApiResponse> {
    const search = new URLSearchParams();

    for (const [key, value] of Object.entries(query)) {
      if ("string" === typeof value && "" !== value.trim()) {
        search.set(key, value.trim());
      }
    }

    const suffix = "" === search.toString() ? "" : `?${search.toString()}`;

    return this.request("GET", `/attachment${suffix}`, null, forwardedHeaders);
  }

  attachAttachment(body: unknown, forwardedHeaders: Record<string, string> = {}): Promise<AttachingApiResponse> {
    return this.request("POST", "/attachment/attach", body, forwardedHeaders);
  }

  detachAttachment(body: unknown, forwardedHeaders: Record<string, string> = {}): Promise<AttachingApiResponse> {
    return this.request("POST", "/attachment/detach", body, forwardedHeaders);
  }

  private async request(method: string, path: string, body: unknown, forwardedHeaders: Record<string, string>): Promise<AttachingApiResponse> {
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

    return await new Promise<AttachingApiResponse>((resolve) => {
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

      if ("" !== payload) {
        request.write(payload);
      }

      request.end();
    });
  }

  private unavailable(): AttachingApiResponse {
    return {
      status: 503,
      body: ATTACHING_API_UNAVAILABLE_PAYLOAD,
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
