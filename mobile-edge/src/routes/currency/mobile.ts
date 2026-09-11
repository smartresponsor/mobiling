import type { FastifyInstance } from "fastify";
import { CurrencingApiClient } from "../../client/currencing/currencingApiClient.js";
import { mobileAccessErrorPayload } from "../../contract/access/error.js";
import { mobileCurrencyCatalogPayload, mobileCurrencySelectorPayload } from "../../contract/currency/index.js";

const currencingApiClient = new CurrencingApiClient();

function forwardedHeaders(request: { headers: Record<string, unknown> }): Record<string, string> {
  const headers: Record<string, string> = {};
  for (const name of ["cookie", "authorization", "x-application-key", "x-application-environment"]) {
    const value = request.headers[name];
    if ("string" === typeof value && "" !== value.trim()) headers[name] = value.trim();
  }
  return headers;
}

function isRecord(value: unknown): value is Record<string, unknown> {
  return null !== value && "object" === typeof value && !Array.isArray(value);
}

function stringValue(value: unknown): string | null {
  return "string" === typeof value && "" !== value.trim() ? value.trim() : null;
}

function errorPayload(body: unknown): Record<string, string> {
  if (isRecord(body) && "string" === typeof body.code && "string" === typeof body.message) {
    return { code: body.code, message: body.message };
  }
  return { code: "currencing_api_error", message: "Currencing API returned an unexpected response." };
}

function querySuffix(query: unknown): string {
  const source = isRecord(query) ? query : {};
  const search = new URLSearchParams();
  const locale = stringValue(source.locale);
  const selectedCode = stringValue(source.selectedCode);
  if (locale) search.set("locale", locale);
  if (selectedCode) search.set("selectedCode", selectedCode);
  return "" === search.toString() ? "" : `?${search.toString()}`;
}

export default async function route(app: FastifyInstance): Promise<void> {
  app.get("/currency/catalog", { schema: { response: { 200: mobileCurrencyCatalogPayload, 500: mobileAccessErrorPayload, 503: mobileAccessErrorPayload } } }, async (request, reply) => {
    const result = await currencingApiClient.get(`/currencing/currencies${querySuffix(request.query)}`, forwardedHeaders(request as { headers: Record<string, unknown> }));
    if (result.status < 200 || result.status >= 300) return reply.code(result.status).send(errorPayload(result.body));
    const body = isRecord(result.body) ? result.body : {};
    return reply.code(200).send({ items: Array.isArray(body.items) ? body.items : [] });
  });

  app.get("/currency/selector", { schema: { response: { 200: mobileCurrencySelectorPayload, 500: mobileAccessErrorPayload, 503: mobileAccessErrorPayload } } }, async (request, reply) => {
    const result = await currencingApiClient.get(`/currencing/currency-selector${querySuffix(request.query)}`, forwardedHeaders(request as { headers: Record<string, unknown> }));
    if (result.status < 200 || result.status >= 300) return reply.code(result.status).send(errorPayload(result.body));
    const body = isRecord(result.body) && isRecord(result.body.item) ? result.body.item : {};
    return reply.code(200).send({
      choices: Array.isArray(body.choices) ? body.choices : [],
      selectedCode: stringValue(body.selectedCode),
      placeholder: stringValue(body.placeholder) ?? "currency.form.placeholder",
    });
  });
}

