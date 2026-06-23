export interface RouteHandlerRequest {
  params: Record<string, unknown>;
  query: Record<string, unknown>;
}

export interface RouteHandlerResponse {
  json(payload: unknown): void;
}
