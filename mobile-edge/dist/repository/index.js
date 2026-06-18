import { RuntimeStore } from "./runtime.js";
let store;
export async function getStore() {
    if (!store) {
        store = new RuntimeStore();
        await store.init();
    }
    return store;
}
