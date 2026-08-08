import type { MessageItem } from '../../../contract/message/thread/MessageItem';

// Marketing America Corp. Oleksandr Tishchenko
export async function listItems(threadId: string): Promise<MessageItem[]> {
  throw new Error(`Message item list is served by the mobile-edge Messaging route for thread ${threadId}.`);
}
