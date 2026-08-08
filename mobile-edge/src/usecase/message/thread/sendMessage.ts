import type { MessageItem } from '../../../contract/message/thread/MessageItem';
import type { SendMessageBody } from '../../../contract/message/thread/SendMessageBody';

// Marketing America Corp. Oleksandr Tishchenko
export async function sendMessage(body: SendMessageBody): Promise<MessageItem> {
  throw new Error(`Message send is served by the mobile-edge Messaging route for thread ${body.threadId}.`);
}
