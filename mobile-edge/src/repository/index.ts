import { RuntimeStore } from "./runtime.js";
import { Storage } from "./storage.js";
let store: Storage | undefined;
export async function getStore(): Promise<Storage> {
  if (!store) { store = new RuntimeStore(); await store.init(); }
  return store;
}
