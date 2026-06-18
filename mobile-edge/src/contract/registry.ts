
type JSONSchema = Record<string, any>;
type Reg = Record<string, Record<string, JSONSchema>>;
const reg: Reg = {
  "receipt.verify": {
    "1": {
      headers:{ type:"object", required:["idempotency-key"], properties:{ "idempotency-key":{type:"string",minLength:8} } },
      body:{ type:"object", additionalProperties:false, required:["transactionId","productId","platform","receipt"],
        properties:{ transactionId:{type:"string",minLength:2}, productId:{type:"string",minLength:1}, platform:{type:"string",enum:["android","ios"]}, receipt:{type:"string",minLength:8} } }
    }
  }
};
export const registry = {
  get(name: string, ver: string){ return (reg[name] || {})[ver]; },
  latest(name: string){ const v = Object.keys(reg[name] || {}).sort(); return v[v.length-1]; }
};
