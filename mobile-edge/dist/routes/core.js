import { coreCall } from "../adapter/http/core.js";
import { adminGuard } from "../middleware/admin.js";
import * as circuit from "../service/circuit.js";
export default async function route(app) {
    app.post("/mobile/core/test", async (request) => coreCall("/test", request.body || {}));
    app.get("/admin/circuit", async (request) => {
        adminGuard(request);
        return circuit.status();
    });
}
