import type { ThreadSummary } from '../../../contract/message/thread/ThreadSummary';

// Marketing America Corp. Oleksandr Tishchenko
export async function listThreads(): Promise<ThreadSummary[]> {
  throw new Error('Message thread list is served by the mobile-edge Messaging route.');
}
