import { ApplicationRuntimeResolver } from "../../runtime/applicationRuntimeResolver.js";
const runtimeResolver = new ApplicationRuntimeResolver();
export default async function route(app) {
    app.get("/application/runtime/:applicationKey/:environment", async (request, reply) => {
        const runtime = await runtimeResolver.resolve(request.params.applicationKey, request.params.environment);
        if (!runtime) {
            return reply.code(503).send({
                code: "application_runtime_unavailable",
                message: "Application runtime manifest is unavailable; clients must retain their configured Mobile Edge fallback.",
            });
        }
        return reply.code(200).send(runtime);
    });
}
