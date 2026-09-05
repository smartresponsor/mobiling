import { LocalizingApiClient } from "../../client/localizing/localizingApiClient.js";
import { mobileLocaleErrorPayload } from "../../contract/locale/error.js";
import { mobileLocaleFallbackPayload, mobileLocaleListPayload, mobileLocaleMessagePayload } from "../../contract/locale/payload.js";
const localizingApiClient = new LocalizingApiClient();
function forwardedHeaders(request) {
    const headers = {};
    const cookie = request.headers.cookie;
    const authorization = request.headers.authorization;
    const applicationKey = request.headers["x-application-key"];
    const applicationEnvironment = request.headers["x-application-environment"];
    if ("string" === typeof cookie && "" !== cookie.trim()) {
        headers.cookie = cookie.trim();
    }
    if ("string" === typeof authorization && "" !== authorization.trim()) {
        headers.authorization = authorization.trim();
    }
    if ("string" === typeof applicationKey && "" !== applicationKey.trim()) {
        headers["x-application-key"] = applicationKey.trim();
    }
    if ("string" === typeof applicationEnvironment && "" !== applicationEnvironment.trim()) {
        headers["x-application-environment"] = applicationEnvironment.trim();
    }
    return headers;
}
function isRecord(value) {
    return null !== value && "object" === typeof value && !Array.isArray(value);
}
function normalizeErrorPayload(body) {
    if (isRecord(body) && "string" === typeof body.code && "string" === typeof body.message) {
        return { code: body.code, message: body.message };
    }
    return { code: "localizing_api_error", message: "Localizing API returned an unexpected response." };
}
function mobileLocaleSchemas() {
    return {
        list: {
            response: {
                200: mobileLocaleListPayload,
                503: mobileLocaleErrorPayload,
            },
        },
        fallback: {
            response: {
                200: mobileLocaleFallbackPayload,
                400: mobileLocaleErrorPayload,
                503: mobileLocaleErrorPayload,
            },
        },
        message: {
            response: {
                200: mobileLocaleMessagePayload,
                400: mobileLocaleErrorPayload,
                404: mobileLocaleErrorPayload,
                503: mobileLocaleErrorPayload,
            },
        },
    };
}
export default async function routeMobileLocale(app) {
    const schemas = mobileLocaleSchemas();
    app.get("/locale", { schema: schemas.list }, async (request, reply) => {
        const result = await localizingApiClient.listLocales(forwardedHeaders(request));
        if (result.status >= 200 && result.status < 300) {
            return reply.code(200).send(result.body);
        }
        return reply.code(result.status).send(normalizeErrorPayload(result.body));
    });
    app.get("/locale/:code/fallback", { schema: schemas.fallback }, async (request, reply) => {
        const { code } = request.params;
        if ("" === code.trim()) {
            return reply.code(400).send({ code: "invalid_locale_code", message: "Locale code is required." });
        }
        const result = await localizingApiClient.resolveFallback(code.trim(), forwardedHeaders(request));
        if (result.status >= 200 && result.status < 300) {
            return reply.code(200).send(normalizeFallbackPayload(result.body));
        }
        return reply.code(result.status).send(normalizeErrorPayload(result.body));
    });
    app.get("/locale/translation/message/resolve", { schema: schemas.message }, async (request, reply) => {
        const query = request.query;
        const locale = (query.locale || "").trim();
        const domain = (query.domain || "").trim();
        const key = (query.key || "").trim();
        if ("" === locale || "" === domain || "" === key) {
            return reply.code(400).send({ code: "invalid_params", message: "locale, domain and key query params are required." });
        }
        const result = await localizingApiClient.resolveMessage(locale, domain, key, forwardedHeaders(request));
        if (result.status >= 200 && result.status < 300) {
            return reply.code(200).send(result.body);
        }
        return reply.code(result.status).send(normalizeErrorPayload(result.body));
    });
}
function normalizeFallbackPayload(body) {
    if (!isRecord(body)) {
        return body;
    }
    if (Array.isArray(body.fallback_chain) && !Object.prototype.hasOwnProperty.call(body, "chain")) {
        return { ...body, chain: body.fallback_chain };
    }
    return body;
}
