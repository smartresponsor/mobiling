import type { MessageItem } from '../../../contract/message/thread/MessageItem';
import type { SendMessageBody } from '../../../contract/message/thread/SendMessageBody';

// Marketing America Corp. Oleksandr Tishchenko
export async function sendMessage(body: SendMessageBody): Promise<MessageItem> {
  return {
    messageId: 'stub-message-id',
    threadId: body.threadId,
    body: body.body,
    senderId: 'stub-sender-id',
    sentAtIso8601: new Date(0).toISOString(),
  };
}
