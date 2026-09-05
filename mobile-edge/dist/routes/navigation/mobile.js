import { NavigatingApiClient } from "../../client/navigating/navigatingApiClient.js";
import { mobileAccessErrorPayload } from "../../contract/access/error.js";
import { mobileNavigationShellPayload } from "../../contract/navigation/shell.js";
const navigatingApiClient = new NavigatingApiClient();
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
        return {
            code: body.code,
            message: body.message,
        };
    }
    return {
        code: "navigating_api_error",
        message: "Navigating API returned an unexpected response.",
    };
}
function navigationShellSchemas() {
    return {
        shell: {
            response: {
                200: mobileNavigationShellPayload,
                500: mobileAccessErrorPayload,
                503: mobileAccessErrorPayload,
            },
        },
    };
}
export default async function route(app) {
    const schemas = navigationShellSchemas();
    app.get("/navigation/mobile/shell", { schema: schemas.shell }, async (request, reply) => {
        const result = await navigatingApiClient.getMobileShell(forwardedHeaders(request));
        if (result.status >= 200 && result.status < 300) {
            return reply.code(200).send(result.body);
        }
        return reply.code(result.status).send(normalizeErrorPayload(result.body));
    });
}
