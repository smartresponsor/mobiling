
import { Pool } from "pg";
import { Storage, ApiKey, DlqItem, DlqSearch } from "./storage.js";
import { ENV } from "../env.js";

export class PgStore implements Storage {
  private pool = new Pool({ connectionString: ENV.DB_URL });
  async init(){
    await this.pool.query(`
      create table if not exists idem (
        key text primary key,
        status integer not null,
        body jsonb not null,
        exp bigint not null
      );
      create table if not exists device (
        deviceId text primary key,
        token text not null,
        platform text not null,
        ts bigint not null
      );
      create table if not exists metric (
        name text not null,
        bucket bigint not null,
        count bigint not null,
        primary key(name, bucket)
      );
      create table if not exists apikey (
        key text primary key,
        name text not null,
        quota_rpm integer not null,
        enabled boolean not null,
        requeue_rpm integer,
        requeue_daily integer
      );
      create table if not exists dlq (
        id bigserial primary key,
        type text not null,
        reason text not null,
        idem text not null,
        apiKey text,
        payload jsonb not null,
        status integer not null,
        attempts integer not null default 0,
        ts bigint not null,
        next bigint not null default 0
      );
      alter table dlq add column if not exists apiKey text;
      alter table apikey add column if not exists requeue_rpm integer;
      alter table apikey add column if not exists requeue_daily integer;
    `);
  }
  // idem
  async idemGet(key:string){
    const r = await this.pool.query("select status, body, exp from idem where key=$1", [key]);
    const row = r.rows[0]; if (!row) return;
    if (Number(row.exp) < Date.now()){ await this.pool.query("delete from idem where key=$1",[key]); return; }
    return { status: row.status, body: row.body };
  }
  async idemPut(key:string, status:number, body:any, ttlMs:number=3600_000){
    await this.pool.query(`insert into idem(key,status,body,exp) values($1,$2,$3,$4)
      on conflict(key) do update set status=excluded.status, body=excluded.body, exp=excluded.exp`, [key, status, body, Date.now()+ttlMs]);
  }
  // device/metrics
  async deviceRegister(deviceId:string, token:string, platform:string){
    await this.pool.query(`insert into device(deviceId,token,platform,ts) values($1,$2,$3,$4)
      on conflict(deviceId) do update set token=excluded.token, platform=excluded.platform, ts=excluded.ts`, [deviceId, token, platform, Date.now()]);
  }
  async deviceCount(){ const r = await this.pool.query("select count(*)::bigint c from device"); return Number(r.rows[0]?.c || 0); }
  async metricIncr(name:string, delta:number=1){
    const bucket = Math.floor(Date.now()/60000);
    await this.pool.query(`insert into metric(name,bucket,count) values($1,$2,$3)
      on conflict(name,bucket) do update set count = metric.count + excluded.count`, [name, bucket, delta]);
  }
  async metricRecent(name:string, limit:number=60){
    const r = await this.pool.query("select bucket::bigint,count::bigint from metric where name=$1 order by bucket desc limit $2", [name, limit]);
    return r.rows as any[];
  }
  // apikey
  async apikeyGet(key:string): Promise<ApiKey | undefined>{
    const r = await this.pool.query("select key,name,quota_rpm,enabled,requeue_rpm,requeue_daily from apikey where key=$1", [key]);
    const row = r.rows[0]; if (!row) return;
    return { key: row.key, name: row.name, quota_rpm: Number(row.quota_rpm), enabled: !!row.enabled, requeue_rpm: row.requeue_rpm, requeue_daily: row.requeue_daily };
  }
  async apikeyUpsert(key:string, name:string, quota_rpm:number, enabled:boolean, requeue_rpm?:number|null, requeue_daily?:number|null){
    await this.pool.query(`insert into apikey(key,name,quota_rpm,enabled,requeue_rpm,requeue_daily) values($1,$2,$3,$4,$5,$6)
      on conflict(key) do update set name=excluded.name, quota_rpm=excluded.quota_rpm, enabled=excluded.enabled, requeue_rpm=excluded.requeue_rpm, requeue_daily=excluded.requeue_daily`, [key, name, quota_rpm, enabled, requeue_rpm ?? null, requeue_daily ?? null]);
  }
  async apikeyList(): Promise<ApiKey[]>{ 
    const r = await this.pool.query("select key,name,quota_rpm,enabled,requeue_rpm,requeue_daily from apikey");
    return r.rows.map(row => ({ key: row.key, name: row.name, quota_rpm: Number(row.quota_rpm), enabled: !!row.enabled, requeue_rpm: row.requeue_rpm, requeue_daily: row.requeue_daily }));
  }
  async apikeyConsume(key:string, amount:number, windowMs:number, limit:number): Promise<boolean>{
    const bucket = Math.floor(Date.now()/windowMs);
    const name = "key:"+key+":"+windowMs;
    await this.pool.query(`insert into metric(name,bucket,count) values($1,$2,$3)
      on conflict(name,bucket) do update set count = metric.count + excluded.count`, [name, bucket, amount]);
    const r = await this.pool.query("select count from metric where name=$1 and bucket=$2", [name, bucket]);
    const count = Number(r.rows[0]?.count || 0);
    return count <= limit;
  }
  async budgetConsume(name:string, windowMs:number, limit:number): Promise<boolean>{
    const bucket = Math.floor(Date.now()/windowMs);
    await this.pool.query(`insert into metric(name,bucket,count) values($1,$2,1)
      on conflict(name,bucket) do update set count = metric.count + 1`, [name, bucket]);
    const r = await this.pool.query("select count from metric where name=$1 and bucket=$2", [name, bucket]);
    const count = Number(r.rows[0]?.count || 0);
    return count <= limit;
  }
  // DLQ
  async dlqPush(type:string, idem:string, payload:any, status:number, reason:string, apiKey?:string|null){
    await this.pool.query("insert into dlq(type,reason,idem,apiKey,payload,status,attempts,ts,next) values($1,$2,$3,$4,$5,0,$6,0)",
      [type, reason, idem, apiKey ?? null, payload, Date.now()]);
  }
  async dlqList(type:string, afterId:number=0, limit:number=100): Promise<DlqItem[]>{
    const r = await this.pool.query("select id,type,reason,idem,apiKey,payload,status,attempts,ts,next from dlq where type=$1 and id>$2 order by id asc limit $3", [type, afterId, limit]);
    return r.rows.map(row => ({ id: Number(row.id), type: row.type, reason: row.reason, idem: row.idem, apiKey: row.apikey, payload: row.payload, status: Number(row.status), attempts: Number(row.attempts || 0), ts: Number(row.ts), next: Number(row.next || 0) }));
  }
  async dlqSearch(type:string, s: DlqSearch): Promise<DlqItem[]>{
    const where = ["type = $1"]; const params:any[] = [type]; let idx = 2;
    function add(cond:string, val:any){ where.push(cond.replace("?", "$"+(idx++))); params.push(val); }
    if (s.afterId && s.afterId > 0){ add("id > ?", s.afterId); }
    if (s.apiKey){ add("apiKey = ?", s.apiKey); }
    if (s.reason){ add("reason = ?", s.reason); }
    if (s.statusMin != null){ add("status >= ?", s.statusMin); }
    if (s.statusMax != null){ add("status <= ?", s.statusMax); }
    if (s.q){ add("payload::text like ?", "%"+s.q+"%"); }
    const limit = Math.min(1000, s.limit || 100);
    const sql = "select id,type,reason,idem,apiKey,payload,status,attempts,ts,next from dlq where " + where.join(" and ") + " order by id asc limit "+limit;
    const r = await this.pool.query(sql, params);
    return r.rows.map(row => ({ id: Number(row.id), type: row.type, reason: row.reason, idem: row.idem, apiKey: row.apikey, payload: row.payload, status: Number(row.status), attempts: Number(row.attempts || 0), ts: Number(row.ts), next: Number(row.next || 0) }));
  }
  async dlqGet(id:number): Promise<DlqItem | undefined>{
    const r = await this.pool.query("select id,type,reason,idem,apiKey,payload,status,attempts,ts,next from dlq where id=$1", [id]);
    const row = r.rows[0]; if (!row) return;
    return { id: Number(row.id), type: row.type, reason: row.reason, idem: row.idem, apiKey: row.apikey, payload: row.payload, status: Number(row.status), attempts: Number(row.attempts || 0), ts: Number(row.ts), next: Number(row.next || 0) };
  }
  async dlqDelete(id:number): Promise<void>{ await this.pool.query("delete from dlq where id=$1", [id]); }
  async dlqDue(type:string, now:number, limit:number): Promise<DlqItem[]>{
    const r = await this.pool.query("select id,type,reason,idem,apiKey,payload,status,attempts,ts,next from dlq where type=$1 and (next=0 or next<=$2) order by id asc limit $3", [type, now, limit]);
    return r.rows.map(row => ({ id: Number(row.id), type: row.type, reason: row.reason, idem: row.idem, apiKey: row.apikey, payload: row.payload, status: Number(row.status), attempts: Number(row.attempts || 0), ts: Number(row.ts), next: Number(row.next || 0) }));
  }
  async dlqUpdateAttempt(id:number, attempts:number, next:number): Promise<void>{
    await this.pool.query("update dlq set attempts=$2, next=$3 where id=$1", [id, attempts, next]);
  }
  async dlqPurgeExpired(beforeTs:number): Promise<number>{
    const r = await this.pool.query("delete from dlq where ts < $1", [beforeTs]);
    return Number(r.rowCount || 0);
  }
}
