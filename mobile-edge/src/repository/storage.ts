
export interface WebhookSubscription { id:string; url:string; secret:string; enabled:boolean; events:string[]; retry_max:number; backoff_base_ms:number; backoff_cap_ms:number; }
export interface WebhookDelivery { id:number; subId:string; event:string; payload:any; status:"pending"|"sent"|"failed"; attempts:number; next:number; ts:number; lastStatus?:number|null; durationMs?:number|null; }
export interface Storage {
  whUpsert(sub:WebhookSubscription): Promise<void>;
  whList(): Promise<WebhookSubscription[]>;
  whFindByEvent(event:string): Promise<WebhookSubscription[]>;
  whEnqueue(subId:string, event:string, payload:any): Promise<number>;
  whDue(now:number, limit:number): Promise<WebhookDelivery[]>;
  whUpdateAttempt(id:number, attempts:number, next:number, lastStatus:number|null, durationMs:number|null): Promise<void>;
  whMarkSent(id:number, lastStatus:number, durationMs:number): Promise<void>;
  whGet(id:number): Promise<WebhookDelivery | undefined>;
  whSearch(event?:string, status?:string, sinceTs?:number, limit?:number): Promise<WebhookDelivery[]>;
}
