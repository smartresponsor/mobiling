
import Database from "better-sqlite3";
import { mkdirSync } from "fs";
import { dirname } from "path";
import { ENV } from "./env.js";

mkdirSync(dirname(ENV.DB_PATH), { recursive: true });
const db = new Database(ENV.DB_PATH);
db.pragma("journal_mode = WAL");
db.exec(`
  create table if not exists idem (
    key text primary key,
    status integer not null,
    body text not null,
    exp integer not null
  );
  create table if not exists device (
    deviceId text primary key,
    token text not null,
    platform text not null,
    ts integer not null
  );
  create table if not exists metric (
    name text not null,
    bucket integer not null,
    count integer not null,
    primary key(name, bucket)
  );
`);

// idempotency
const idemGetStmt = db.prepare("select status, body, exp from idem where key = ?");
const idemPutStmt = db.prepare("insert into idem(key,status,body,exp) values(?,?,?,?) on conflict(key) do update set status=excluded.status, body=excluded.body, exp=excluded.exp");
const idemSweepStmt = db.prepare("delete from idem where exp < ?");
export function idemGet(key:string): {status:number, body:any} | undefined {
  const row = idemGetStmt.get(key) as any;
  if (!row) return;
  if (row.exp < Date.now()){ db.prepare("delete from idem where key=?").run(key); return; }
  return { status: row.status, body: JSON.parse(row.body) };
}
export function idemPut(key:string, status:number, body:any, ttlMs:number = 3600_000){
  idemPutStmt.run(key, status, JSON.stringify(body), Date.now() + ttlMs);
}
export function idemSweep(){ idemSweepStmt.run(Date.now()); }
setInterval(idemSweep, 60_000).unref();

// device
const devUpsert = db.prepare("insert into device(deviceId,token,platform,ts) values(?,?,?,?) on conflict(deviceId) do update set token=excluded.token, platform=excluded.platform, ts=excluded.ts");
export function deviceRegister(deviceId:string, token:string, platform:string){
  devUpsert.run(deviceId, token, platform, Date.now());
}
export function deviceCount(): number {
  const r = db.prepare("select count(*) c from device").get() as any;
  return r.c || 0;
}

// metrics
const metricUpsert = db.prepare("insert into metric(name,bucket,count) values(?,?,?) on conflict(name,bucket) do update set count = count + excluded.count");
export function metricIncr(name:string, delta:number=1){
  const bucket = Math.floor(Date.now()/60000); // minute bucket
  metricUpsert.run(name, bucket, delta);
}
export function metricRecent(name:string, limit:number=60){
  return db.prepare("select bucket,count from metric where name=? order by bucket desc limit ?").all(name, limit) as any[];
}

export default db;
