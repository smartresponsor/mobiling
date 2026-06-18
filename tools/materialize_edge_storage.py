#!/usr/bin/env python3
"""Materialize a complete process-local Mobile Edge storage contract."""
from pathlib import Path
from textwrap import dedent
import json

ROOT = Path(__file__).resolve().parents[1]

def write(relative: str, text: str) -> None:
    path = ROOT / relative
    path.parent.mkdir(parents=True, exist_ok=True)
    path.write_text(dedent(text).lstrip("\n").rstrip() + "\n", encoding="utf-8")

def main() -> None:
    package_path = ROOT / "mobile-edge/package.json"
    package = json.loads(package_path.read_text(encoding="utf-8"))
    for name in ("better-sqlite3", "pg"):
        package.get("dependencies", {}).pop(name, None)
    for name in ("@types/better-sqlite3", "@types/pg"):
        package.get("devDependencies", {}).pop(name, None)
    package_path.write_text(json.dumps(package, indent=2) + "\n", encoding="utf-8")

    write("mobile-edge/src/repository/storage.ts", '''
        export interface ApiKey { key: string; name: string; quota_rpm: number; enabled: boolean; requeue_rpm?: number | null; requeue_daily?: number | null; }
        export interface DlqItem { id: number; type: string; reason: string; idem: string; apiKey?: string | null; payload: unknown; status: number; attempts: number; ts: number; next: number; }
        export interface DlqSearch { afterId?: number; limit?: number; apiKey?: string; reason?: string; statusMin?: number; statusMax?: number; q?: string; }
        export interface WebhookSubscription { id: string; url: string; secret: string; enabled: boolean; events: string[]; retry_max: number; backoff_base_ms: number; backoff_cap_ms: number; }
        export interface WebhookDelivery { id: number; subId: string; event: string; payload: unknown; status: "pending" | "sent" | "failed"; attempts: number; next: number; ts: number; lastStatus?: number | null; durationMs?: number | null; }
        export interface Storage {
          init(): Promise<void>;
          idemGet(key: string): Promise<{ status: number; body: unknown } | undefined>;
          idemPut(key: string, status: number, body: unknown, ttlMs?: number): Promise<void>;
          deviceRegister(deviceId: string, token: string, platform: string): Promise<void>;
          deviceCount(): Promise<number>;
          metricIncr(name: string, delta?: number): Promise<void>;
          metricRecent(name: string, limit?: number): Promise<Array<{ bucket: number; count: number }>>;
          apikeyGet(key: string): Promise<ApiKey | undefined>;
          apikeyUpsert(key: string, name: string, quotaRpm: number, enabled: boolean, requeueRpm?: number | null, requeueDaily?: number | null): Promise<void>;
          apikeyList(): Promise<ApiKey[]>;
          apikeyConsume(key: string, amount: number, windowMs: number, limit: number): Promise<boolean>;
          budgetConsume(name: string, windowMs: number, limit: number): Promise<boolean>;
          dlqPush(type: string, idem: string, payload: unknown, status: number, reason: string, apiKey?: string | null): Promise<void>;
          dlqList(type: string, afterId?: number, limit?: number): Promise<DlqItem[]>;
          dlqSearch(type: string, search: DlqSearch): Promise<DlqItem[]>;
          dlqGet(id: number): Promise<DlqItem | undefined>;
          dlqDelete(id: number): Promise<void>;
          dlqDue(type: string, now: number, limit: number): Promise<DlqItem[]>;
          dlqUpdateAttempt(id: number, attempts: number, next: number): Promise<void>;
          dlqPurgeExpired(beforeTs: number): Promise<number>;
          whUpsert(subscription: WebhookSubscription): Promise<void>;
          whList(): Promise<WebhookSubscription[]>;
          whFindByEvent(event: string): Promise<WebhookSubscription[]>;
          whEnqueue(subscriptionId: string, event: string, payload: unknown): Promise<number>;
          whDue(now: number, limit: number): Promise<WebhookDelivery[]>;
          whUpdateAttempt(id: number, attempts: number, next: number, lastStatus: number | null, durationMs: number | null): Promise<void>;
          whMarkSent(id: number, lastStatus: number, durationMs: number): Promise<void>;
          whGet(id: number): Promise<WebhookDelivery | undefined>;
          whSearch(event?: string, status?: string, sinceTs?: number, limit?: number): Promise<WebhookDelivery[]>;
        }
    ''')
    write("mobile-edge/src/repository/runtime.ts", '''
        import { ApiKey, DlqItem, DlqSearch, Storage, WebhookDelivery, WebhookSubscription } from "./storage.js";

        type Idempotency = { status: number; body: unknown; expiresAt: number };
        type Device = { token: string; platform: string; updatedAt: number };

        export class RuntimeStore implements Storage {
          private readonly idempotency = new Map<string, Idempotency>();
          private readonly device = new Map<string, Device>();
          private readonly metric = new Map<string, Map<number, number>>();
          private readonly apiKey = new Map<string, ApiKey>();
          private readonly dlq: DlqItem[] = [];
          private readonly webhook = new Map<string, WebhookSubscription>();
          private readonly delivery: WebhookDelivery[] = [];
          private dlqSequence = 0;
          private deliverySequence = 0;

          async init(): Promise<void> {}
          async idemGet(key: string) { const item = this.idempotency.get(key); if (!item || item.expiresAt < Date.now()) { this.idempotency.delete(key); return undefined; } return { status: item.status, body: item.body }; }
          async idemPut(key: string, status: number, body: unknown, ttlMs = 3_600_000) { this.idempotency.set(key, { status, body, expiresAt: Date.now() + ttlMs }); }
          async deviceRegister(deviceId: string, token: string, platform: string) { this.device.set(deviceId, { token, platform, updatedAt: Date.now() }); }
          async deviceCount() { return this.device.size; }
          async metricIncr(name: string, delta = 1) { const bucket = Math.floor(Date.now() / 60_000); const series = this.metric.get(name) || new Map<number, number>(); series.set(bucket, (series.get(bucket) || 0) + delta); this.metric.set(name, series); }
          async metricRecent(name: string, limit = 60) { return [...(this.metric.get(name) || new Map()).entries()].sort(([left], [right]) => right - left).slice(0, limit).map(([bucket, count]) => ({ bucket, count })); }
          async apikeyGet(key: string) { return this.apiKey.get(key); }
          async apikeyUpsert(key: string, name: string, quota_rpm: number, enabled: boolean, requeue_rpm?: number | null, requeue_daily?: number | null) { this.apiKey.set(key, { key, name, quota_rpm, enabled, requeue_rpm, requeue_daily }); }
          async apikeyList() { return [...this.apiKey.values()]; }
          async apikeyConsume(key: string, amount: number, windowMs: number, limit: number) { return this.consume(`key:${key}`, amount, windowMs, limit); }
          async budgetConsume(name: string, windowMs: number, limit: number) { return this.consume(name, 1, windowMs, limit); }
          async dlqPush(type: string, idem: string, payload: unknown, status: number, reason: string, apiKey?: string | null) { this.dlq.push({ id: ++this.dlqSequence, type, reason, idem, apiKey, payload, status, attempts: 0, ts: Date.now(), next: 0 }); }
          async dlqList(type: string, afterId = 0, limit = 100) { return this.dlq.filter((item) => item.type === type && item.id > afterId).slice(0, limit); }
          async dlqSearch(type: string, search: DlqSearch) { return this.dlq.filter((item) => item.type === type && (!search.afterId || item.id > search.afterId) && (!search.apiKey || item.apiKey === search.apiKey) && (!search.reason || item.reason === search.reason) && (search.statusMin === undefined || item.status >= search.statusMin) && (search.statusMax === undefined || item.status <= search.statusMax) && (!search.q || JSON.stringify(item.payload).includes(search.q))).slice(0, Math.min(1000, search.limit || 100)); }
          async dlqGet(id: number) { return this.dlq.find((item) => item.id === id); }
          async dlqDelete(id: number) { const index = this.dlq.findIndex((item) => item.id === id); if (index >= 0) this.dlq.splice(index, 1); }
          async dlqDue(type: string, now: number, limit: number) { return this.dlq.filter((item) => item.type === type && (item.next === 0 || item.next <= now)).slice(0, limit); }
          async dlqUpdateAttempt(id: number, attempts: number, next: number) { const item = await this.dlqGet(id); if (item) { item.attempts = attempts; item.next = next; } }
          async dlqPurgeExpired(beforeTs: number) { const initial = this.dlq.length; for (let index = this.dlq.length - 1; index >= 0; index--) if (this.dlq[index].ts < beforeTs) this.dlq.splice(index, 1); return initial - this.dlq.length; }
          async whUpsert(subscription: WebhookSubscription) { this.webhook.set(subscription.id, subscription); }
          async whList() { return [...this.webhook.values()]; }
          async whFindByEvent(event: string) { return [...this.webhook.values()].filter((item) => item.enabled && item.events.includes(event)); }
          async whEnqueue(subId: string, event: string, payload: unknown) { const id = ++this.deliverySequence; this.delivery.push({ id, subId, event, payload, status: "pending", attempts: 0, next: 0, ts: Date.now() }); return id; }
          async whDue(now: number, limit: number) { return this.delivery.filter((item) => item.status !== "sent" && (item.next === 0 || item.next <= now)).slice(0, limit); }
          async whUpdateAttempt(id: number, attempts: number, next: number, lastStatus: number | null, durationMs: number | null) { const item = await this.whGet(id); if (item) { item.attempts = attempts; item.next = next; item.lastStatus = lastStatus; item.durationMs = durationMs; item.status = "failed"; } }
          async whMarkSent(id: number, lastStatus: number, durationMs: number) { const item = await this.whGet(id); if (item) { item.status = "sent"; item.lastStatus = lastStatus; item.durationMs = durationMs; } }
          async whGet(id: number) { return this.delivery.find((item) => item.id === id); }
          async whSearch(event?: string, status?: string, sinceTs?: number, limit = 200) { return this.delivery.filter((item) => (!event || item.event === event) && (!status || item.status === status) && (!sinceTs || item.ts >= sinceTs)).slice(-Math.min(1000, limit)).reverse(); }
          private async consume(name: string, amount: number, windowMs: number, limit: number) { const bucket = Math.floor(Date.now() / windowMs); const series = this.metric.get(name) || new Map<number, number>(); const count = (series.get(bucket) || 0) + amount; series.set(bucket, count); this.metric.set(name, series); return count <= limit; }
        }
    ''')
    write("mobile-edge/src/repository/index.ts", '''
        import { RuntimeStore } from "./runtime.js";
        import { Storage } from "./storage.js";
        let store: Storage | undefined;
        export async function getStore(): Promise<Storage> {
          if (!store) { store = new RuntimeStore(); await store.init(); }
          return store;
        }
    ''')
    for relative in ("mobile-edge/src/repository/sqlite.ts", "mobile-edge/src/repository/pg.ts", "mobile-edge/src/db.ts"):
        path = ROOT / relative
        if path.exists(): path.unlink()
    print("Mobile Edge storage materialized")

if __name__ == "__main__":
    main()
