import { CrudingApiClient } from "../../client/cruding/crudingApiClient.js";
const client = new CrudingApiClient();
const allowed = new Set(["retail", "order", "project", "wallet", "account", "reservation", "funding", "withdrawal", "payment-instrument"]);
function forwarded(headers) {
    const result = {};
    for (const key of ["cookie", "authorization"]) {
        const value = headers[key];
        if (typeof value === "string" && value.trim())
            result[key] = value.trim();
    }
    return result;
}
export default async function route(app) {
    app.route({
        method: ["GET", "POST", "PUT", "PATCH", "DELETE"],
        url: "/my/:resource/:identity?",
        handler: async (request, reply) => {
            const params = request.params;
            const resource = (params.resource || "").trim();
            if (!allowed.has(resource))
                return reply.code(404).send({ code: "crud_resource_not_allowed", message: "Mobile CRUD resource is not allowed." });
            const result = await client.request(request.method, resource, params.identity?.trim() || null, request.body ?? null, forwarded(request.headers));
            return reply.code(result.status).send(result.body);
        },
    });
    app.route({
        method: ["GET", "POST"],
        url: "/retail/:retailId/:step",
        handler: async (request, reply) => {
            const params = request.params;
            const retailId = (params.retailId || "").trim();
            const step = (params.step || "").trim();
            const allowedSteps = new Set(["placement", "fulfillment", "location", "pricing", "publish"]);
            if (!/^[1-9][0-9]*$/.test(retailId) || !allowedSteps.has(step)) {
                return reply.code(404).send({ code: "retail_placement_route_not_found", message: "Retail placement route is not available." });
            }
            if ((step === "placement" && request.method !== "GET") || (step !== "placement" && request.method !== "POST")) {
                return reply.code(405).send({ code: "retail_placement_method_not_allowed", message: "Retail placement method is not allowed." });
            }
            const result = await client.requestPath(request.method, `/api/retail/${encodeURIComponent(retailId)}/${step}`, request.body ?? null, forwarded(request.headers));
            return reply.code(result.status).send(result.body);
        },
    });
}
