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
