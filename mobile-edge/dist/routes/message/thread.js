import { listItems } from '../../usecase/message/thread/listItems';
import { listThreads } from '../../usecase/message/thread/listThreads';
import { sendMessage } from '../../usecase/message/thread/sendMessage';
// Marketing America Corp. Oleksandr Tishchenko
export function buildMessageThreadRoutes() {
    return {
        index: '/message/thread',
        detail: '/message/thread/:threadId',
        async listThreads() {
            return listThreads();
        },
        async listItems(threadId) {
            return listItems(threadId);
        },
        async sendMessage(body) {
            return sendMessage(body);
        },
    };
}
