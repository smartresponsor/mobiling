
import { ENV } from "../env.js";
import { Storage } from "./storage.js";
import { SqliteStore } from "./sqlite.js";
// PG реализацию можно подключить по ENV.STORE === 'pg'
let store: Storage;
export async function getStore(): Promise<Storage> {
  if (!store){
    store = new SqliteStore();
    await (store as any).init();
  }
  return store;
}
