
import { createHmac, randomBytes, createPrivateKey, sign as csign } from "crypto";
export function hmacHex(secret: string, body: string | Buffer): string {
  return createHmac("sha256", secret).update(body).digest("hex");
}
export function randId(n=16){ return randomBytes(n).toString("hex"); }
export function signEd25519(pem: string, data: Buffer){
  try {
    const key = createPrivateKey(pem);
    return csign(null, data, key); // Ed25519
  } catch { return Buffer.alloc(0); }
}
