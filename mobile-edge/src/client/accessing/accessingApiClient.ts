import { request as httpRequest } from "http";
import { request as httpsRequest } from "https";
import { ENV } from "../../env.js";

export interface AccessingApiSignInRequest {
  email: string;
  password: string;
}

export interface AccessingApiRegisterRequest {
  displayName: string;
  email: string;
  password: string;
}

export interface AccessingApiIdentityPayload {
  userId: string | number;
  accountId?: string | null;
  displayName: string;
  email: string;
  emailVerified: boolean;
  secondFactorEnabled: boolean;
}

export interface AccessingApiSessionPayload {
  status: string;
  identity: AccessingApiIdentityPayload | null;
  accessToken: string | null;
  refreshToken: string | null;
  expiresAt: string | null;
  requiresVerification: boolean;
  requiresSecondFactor: boolean;
}

export interface AccessingApiErrorPayload {
  code: string;
  message: string;
  fieldErrors?: Record<string, string[]> | null;
}

export interface AccessingApiResponse {
  status: number;
  body: unknown;
  responseCookie: string[];
}

const ACCESSING_API_UNAVAILABLE_PAYLOAD: AccessingApiErrorPayload = {
  code: "accessing_api_unavailable",
  message: "Accessing API is unavailable from mobile-edge.",
};

export class AccessingApiClient {
  constructor(
    private readonly baseUrl: string = ENV.ACCESSING_API_BASE_URL,
    private readonly timeoutMs: number = ENV.ACCESSING_API_TIMEOUT_MS,
  ) {
  }

  signIn(request: AccessingApiSignInRequest, forwardedHeaders: Record<string, string> = {}): Promise<AccessingApiResponse> {
    return this.request("POST", "/api/access/signin", request, forwardedHeaders);
  }

  register(request: AccessingApiRegisterRequest, forwardedHeaders: Record<string, string> = {}): Promise<AccessingApiResponse> {
    return this.request("POST", "/api/access/register", request, forwardedHeaders);
  }

  logout(forwardedHeaders: Record<string, string> = {}): Promise<AccessingApiResponse> {
    return this.request("POST", "/api/access/logout", null, forwardedHeaders);
  }

  session(forwardedHeaders: Record<string, string> = {}): Promise<AccessingApiResponse> {
    return this.request("GET", "/api/access/session", null, forwardedHeaders);
  }

  private async request(method: string, path: string, body: unknown, forwardedHeaders: Record<string, string>): Promise<AccessingApiResponse> {
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

    return await new Promise<AccessingApiResponse>((resolve) => {
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
            const responseCookie = response.headers["set-cookie"] || [];
            resolve({
              status: response.statusCode || 502,
              body: this.parseResponseBody(text),
              responseCookie: Array.isArray(responseCookie) ? responseCookie : [responseCookie],
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

  private unavailable(): AccessingApiResponse {
    return {
      status: 503,
      body: ACCESSING_API_UNAVAILABLE_PAYLOAD,
      responseCookie: [],
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
