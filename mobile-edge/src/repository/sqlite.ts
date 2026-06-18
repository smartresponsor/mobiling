
import Database from "better-sqlite3";
import { mkdirSync } from "fs";
import { dirname } from "path";
import { ENV } from "../env.js";
import { Storage, WebhookSubscription, WebhookDelivery } from "./storage.js";

export class SqliteStore implements Storage {
  private db: Database.Database;
  constructor(){
    mkdirSync(dirname(ENV.DB_PATH), { recursive: true });
    this.db = new Database(ENV.DB_PATH);
    this.db.pragma("journal_mode = WAL");
  }
  async init(){
    this.db.exec(`
      create table if not exists wh_sub (id text primary key, url text not null, secret text not null, enabled integer not null, events text not null, retry_max integer not null, backoff_base_ms integer not null, backoff_cap_ms integer not null);
      create table if not exists wh_delivery (id integer primary key autoincrement, subId text not null, event text not null, payload text not null, status text not null, attempts integer not null default 0, next integer not null default 0, ts integer not null, lastStatus integer, durationMs integer);
    `);
    const ins = this.db.prepare("insert into wh_sub(id,url,secret,enabled,events,retry_max,backoff_base_ms,backoff_cap_ms) values(?,?,?,?,?,?,?,?) on conflict(id) do nothing");
    if (ENV.WEBHOOK_URL_REQUEUE) ins.run("env-requeue", ENV.WEBHOOK_URL_REQUEUE, ENV.WEBHOOK_SECRET, 1, JSON.stringify(["dlq.requeue"]), 12, 1000, 600000);
    if (ENV.WEBHOOK_URL_PURGE) ins.run("env-purge", ENV.WEBHOOK_URL_PURGE, ENV.WEBHOOK_SECRET, 1, JSON.stringify(["dlq.purge"]), 4, 1000, 600000);
  }
  async whUpsert(sub:WebhookSubscription){ this.db.prepare("insert into wh_sub(id,url,secret,enabled,events,retry_max,backoff_base_ms,backoff_cap_ms) values(?,?,?,?,?,?,?,?) on conflict(id) do update set url=excluded.url, secret=excluded.secret, enabled=excluded.enabled, events=excluded.events, retry_max=excluded.retry_max, backoff_base_ms=excluded.backoff_base_ms, backoff_cap_ms=excluded.backoff_cap_ms").run(sub.id, sub.url, sub.secret, sub.enabled?1:0, JSON.stringify(sub.events||[]), sub.retry_max, sub.backoff_base_ms, sub.backoff_cap_ms); }
  async whList(){ return this.db.prepare("select id,url,secret,enabled,events,retry_max,backoff_base_ms,backoff_cap_ms from wh_sub").all().map((r:any)=>({ id:r.id,url:r.url,secret:r.secret,enabled:!!r.enabled,events:JSON.parse(r.events||"[]"),retry_max:r.retry_max,backoff_base_ms:r.backoff_base_ms,backoff_cap_ms:r.backoff_cap_ms })); }
  async whFindByEvent(event:string){ return (await this.whList()).filter(s=>s.enabled && s.events.includes(event)); }
  async whEnqueue(subId:string,event:string,payload:any){ const info=this.db.prepare("insert into wh_delivery(subId,event,payload,status,attempts,next,ts) values(?,?,?,?,0,0,?)").run(subId,event,JSON.stringify(payload),"pending",Date.now()); return Number(info.lastInsertRowid); }
  async whDue(now:number,limit:number){ const rows:any[]=this.db.prepare("select id,subId,event,payload,status,attempts,next,ts,lastStatus,durationMs from wh_delivery where status!='sent' and (next=0 or next<=?) order by id asc limit ?").all(now,limit); return rows.map(r=>({ id:r.id,subId:r.subId,event:r.event,payload:JSON.parse(r.payload),status:r.status,attempts:r.attempts,next:r.next,ts:r.ts,lastStatus:r.lastStatus,durationMs:r.durationMs })); }
  async whUpdateAttempt(id:number,attempts:number,next:number,lastStatus:number|null,durationMs:number|null){ this.db.prepare("update wh_delivery set attempts=?, next=?, lastStatus=?, durationMs=? where id=?").run(attempts,next,lastStatus,durationMs,id); }
  async whMarkSent(id:number,lastStatus:number,durationMs:number){ this.db.prepare("update wh_delivery set status='sent', lastStatus=?, durationMs=? where id=?").run(lastStatus,durationMs,id); }
  async whGet(id:number){ const r:any=this.db.prepare("select id,subId,event,payload,status,attempts,next,ts,lastStatus,durationMs from wh_delivery where id=?").get(id); if(!r) return; return { id:r.id,subId:r.subId,event:r.event,payload:JSON.parse(r.payload),status:r.status,attempts:r.attempts,next:r.next,ts:r.ts,lastStatus:r.lastStatus,durationMs:r.durationMs }; }
  async whSearch(event?:string,status?:string,sinceTs?:number,limit:number=200){ const where:string[]=[]; const params:any[]=[]; if(event){where.push("event=?");params.push(event);} if(status){where.push("status=?");params.push(status);} if(sinceTs){where.push("ts>=?");params.push(sinceTs);} const sql="select id,subId,event,payload,status,attempts,next,ts,lastStatus,durationMs from wh_delivery"+(where.length?" where "+where.join(" and "):"")+" order by id desc limit ?"; params.push(limit); const rows:any[]=this.db.prepare(sql).all(...params); return rows.map(r=>({ id:r.id,subId:r.subId,event:r.event,payload:JSON.parse(r.payload),status:r.status,attempts:r.attempts,next:r.next,ts:r.ts,lastStatus:r.lastStatus,durationMs:r.durationMs })); }
}
