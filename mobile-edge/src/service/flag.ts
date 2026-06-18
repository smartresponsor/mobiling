
import { createHash } from "crypto";
export type Context = { userId?:string; app?:string; os?:string; version?:string; };
export type Rule = { key:string; pct?:number; app?:string; os?:string; versionMin?:string; };
export type Snapshot = { flags: Record<string, Rule> };
export function evalFlag(s: Snapshot, key:string, ctx: Context){
  const r = s.flags[key]; if (!r) return false;
  if (r.app && ctx.app && r.app !== ctx.app) return false;
  if (r.os && ctx.os && r.os !== ctx.os) return false;
  if (r.versionMin && ctx.version && ctx.version < r.versionMin) return false;
  if (typeof r.pct === "number"){
    const h = createHash("sha256").update((ctx.userId||"") + ":" + key).digest("hex");
    const n = parseInt(h.slice(0,8),16) % 10000; return n < Math.floor(r.pct*100);
  }
  return true;
}
