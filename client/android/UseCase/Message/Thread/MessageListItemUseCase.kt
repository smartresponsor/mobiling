package app.mobiling.client.usecase.message.thread

import app.mobiling.client.contract.message.thread.MessageItemPayload
import app.mobiling.client.data.message.thread.MessageThreadGateway

/**
 * Marketing America Corp. Oleksandr Tishchenko
 */
class MessageListItemUseCase(
    private val gateway: MessageThreadGateway,
) {
    suspend operator fun invoke(threadId: String): List<MessageItemPayload> = gateway.listItems(threadId)
}
