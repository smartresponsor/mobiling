
import { FastifyInstance } from "fastify";
import { coreCall } from "../adapter/http/core.js";
import * as CB from "../service/circuit.js";
export default async function route(app: FastifyInstance){
  app.post("/mobile/core/test", async (req:any)=>{
    return await coreCall("/test", req.body||{});
  });
  app.get("/admin/circuit", async ()=> CB.status());
}
