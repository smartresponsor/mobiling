
import * as jose from "jose";
import { ENV } from "../env.js";
const alg = "HS256";
function key(){ return new TextEncoder().encode(ENV.JWT_SECRET); }
function prevKey(){ return ENV.JWT_PREV_SECRET ? new TextEncoder().encode(ENV.JWT_PREV_SECRET) : undefined; }
export async function signJwt(payload: Record<string, any>){
  const now = Math.floor(Date.now()/1000);
  return await new jose.SignJWT(payload).setProtectedHeader({ alg }).setIssuedAt(now).setExpirationTime(now + ENV.JWT_TTL_SEC).sign(key());
}
export async function verifyJwt(token: string){
  try {
    const { payload } = await jose.jwtVerify(token, key(), { algorithms: ["HS256"] });
    return payload;
  } catch (e){
    const pk = prevKey();
    if (!pk) throw e;
    if (ENV.JWT_PREV_EXP_TS && Date.now() > ENV.JWT_PREV_EXP_TS) throw e;
    const { payload } = await jose.jwtVerify(token, pk, { algorithms: ["HS256"] });
    return payload;
  }
}
