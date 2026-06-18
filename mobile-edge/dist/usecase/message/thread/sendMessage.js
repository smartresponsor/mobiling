// Marketing America Corp. Oleksandr Tishchenko
export async function sendMessage(body) {
    return {
        messageId: 'stub-message-id',
        threadId: body.threadId,
        body: body.body,
        senderId: 'stub-sender-id',
        sentAtIso8601: new Date(0).toISOString(),
    };
}
