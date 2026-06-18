
import { FastifyInstance } from "fastify";
import { ENV } from "../env.js";
import { readFileSync, writeFileSync, existsSync, mkdirSync } from "fs";
import { join } from "path";

function guard(req:any,res:any){ if (req.headers["x-admin-token"] !== ENV.ADMIN_TOKEN){ res.status(403).send({error:"forbidden"}); return false; } return true; }
const DIR = "data/backup";

export default async function route(app: FastifyInstance){
  app.get("/admin/backup", async (req:any, res)=>{
    if (!guard(req,res)) return;
    try{
      const p = join(DIR, "config.json");
      const txt = existsSync(p) ? readFileSync(p,"utf8") : "{}";
      res.type("application/json").send(txt);
    }catch{ res.status(500).send({error:"io"}); }
  });
  app.post("/admin/backup", async (req:any, res)=>{
    if (!guard(req,res)) return;
    try{
      mkdirSync(DIR, { recursive: true });
      writeFileSync(join(DIR,"config.json"), JSON.stringify(req.body||{}, null, 2), "utf8");
      res.send({ok:true});
    }catch{ res.status(500).send({error:"io"}); }
  });
}
