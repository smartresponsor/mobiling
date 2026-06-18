import { getStore } from './repository/index.js';
export async function publish(event, payload) {
    const store = await getStore();
    const subscription = await store.whFindByEvent(event);
    for (const item of subscription) {
        await store.whEnqueue(item.id, event, payload);
    }
}
export async function hookRequeue(payload) {
    await publish('dlq.requeue', payload);
}
export async function hookPurge(payload) {
    await publish('dlq.purge', payload);
}
