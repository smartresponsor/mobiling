
import { FastifyInstance } from "fastify";
import { M } from "../metrics.js";
export default async function route(app: FastifyInstance){
  await M.route(app);
}
