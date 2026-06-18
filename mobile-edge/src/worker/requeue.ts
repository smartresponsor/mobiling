
import { ENV } from "../env.js";
import { getStore } from "../repository/index.js";
import { verifyReceiptViaCore } from "../adapter/http/commerce.js";
import { hookRequeue, hookPurge } from "../webhook.js";
import { M } from "../metrics.js";

function jitter(v:number){ return Math.floor(v * (0.5 + Math.random())); }
function nextDelayMs(attempts:number){
  const base = ENV.REQUEUE_BASE_MS;
  const cap = ENV.REQUEUE_CAP_MS;
  let d = base * Math.pow(2, attempts);
  if (d > cap) d = cap;
  return jitter(d);
}
let inFlight = 0;
let lastMinute = 0; let minuteCount = 0;
let lastDay = 0; let dayCount = 0;
function globalBudgetOk(): boolean {
  const now = Date.now();
  const m = Math.floor(now / 60000);
  const d = Math.floor(now / 86400000);
  if (m !== lastMinute){ lastMinute = m; minuteCount = 0; }
  if (d !== lastDay){ lastDay = d; dayCount = 0; }
  if (minuteCount >= ENV.REQUEUE_RPM) return false;
  if (dayCount >= ENV.REQUEUE_DAILY) return false;
  minuteCount++; dayCount++;
  return true;
}

async function perKeyBudgetOk(key?: string | null): Promise<boolean>{
  if (!key) return true;
  const s = await getStore();
  const ak = await s.apikeyGet(key);
  if (!ak) return true;
  const rpm = ak.requeue_rpm ?? 0;
  const daily = ak.requeue_daily ?? 0;
  let minuteOk = true, dayOk = true;
  if (rpm && rpm > 0){
    minuteOk = await s.budgetConsume("requeue:min:"+key, 60000, rpm);
  }
  if (daily && daily > 0){
    dayOk = await s.budgetConsume("requeue:day:"+key, 86400000, daily);
  }
  if (!(minuteOk && dayOk)){ await M.inc("budgetDenied"); }
  return minuteOk && dayOk;
}

export function startRequeue(){
  if (!ENV.REQUEUE_ENABLED) return;
  setInterval(async () => {
    const s = await getStore();
    // TTL purge (+ webhook + metrics)
    const before = Date.now() - (ENV.DLQ_TTL_DAYS * 86400000);
    const purged = await s.dlqPurgeExpired(before);
    if (purged > 0){
      await M.inc("dlqPurged");
      await hookPurge({ count: purged });
    }
    if (inFlight >= ENV.REQUEUE_CONCURRENCY) return;
    const due = await s.dlqDue("receipt", Date.now(), ENV.REQUEUE_CONCURRENCY - inFlight);
    for (const item of due){
      if (inFlight >= ENV.REQUEUE_CONCURRENCY) break;
      if (!(await perKeyBudgetOk(item.apiKey))) continue;
      if (!globalBudgetOk()) break;
      inFlight++;
      (async () => {
        try {
          const out = await verifyReceiptViaCore(item.payload);
          if (out.status >= 400){
            await s.dlqUpdateAttempt(item.id, item.attempts + 1, Date.now() + nextDelayMs(item.attempts + 1));
            await M.inc("dlqRequeue", { mode:"auto", result:"fail" });
            await hookRequeue({ mode:"auto", id:item.id, reason:item.reason, apiKey:item.apiKey||null, attempts:item.attempts+1, status:out.status });
          } else {
            await s.dlqDelete(item.id);
            await M.inc("dlqRequeue", { mode:"auto", result:"success" });
            await hookRequeue({ mode:"auto", id:item.id, reason:item.reason, apiKey:item.apiKey||null, attempts:item.attempts, status:out.status });
          }
        } catch {
          await s.dlqUpdateAttempt(item.id, item.attempts + 1, Date.now() + nextDelayMs(item.attempts + 1));
          await M.inc("dlqRequeue", { mode:"auto", result:"fail" });
          await hookRequeue({ mode:"auto", id:item.id, reason:item.reason, apiKey:item.apiKey||null, attempts:item.attempts+1, status:502 });
        } finally {
          inFlight--;
        }
      })();
    }
  }, 1000).unref();
}
