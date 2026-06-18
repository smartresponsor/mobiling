
export class EdgeClient {
  constructor(private base:string, private bearer?:string){}
  private async req(path:string, opt:RequestInit={}){
    const url = this.base.replace(/\/$/,'') + path;
    opt.headers = Object.assign({"Content-Type":"application/json"}, opt.headers||{});
    if (this.bearer) (opt.headers as any)["Authorization"] = this.bearer;
    const r = await fetch(url, opt);
    if (!r.ok) throw new Error(`HTTP ${r.status}`);
    const txt = await r.text();
    try { return JSON.parse(txt); } catch { return txt; }
  }
  config(){ return this.req("/mobile/config"); }
  entitlement(){ return this.req("/mobile/entitlement"); }
  pushRegister(deviceId:string, token:string, platform?:string){
    return this.req("/mobile/push/register", { method:"POST", body: JSON.stringify({ deviceId, token, platform }) });
  }
  receiptVerify(idemKey:string, body:{transactionId:string,productId:string,platform:"android"|"ios",receipt:string}){
    return this.req("/mobile/receipt/verify", { method:"POST", body: JSON.stringify(body), headers: {"Idempotency-Key": idemKey } });
  }
  analyticBatch(signature:string, events:any[]){
    const body = JSON.stringify({ events });
    return this.req("/mobile/analytic/batch", { method:"POST", body, headers: {"X-SR-Analytic-Signature": signature} });
  }
}
