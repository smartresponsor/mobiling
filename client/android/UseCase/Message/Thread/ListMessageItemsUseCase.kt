package app.mobiling.client.usecase.message.thread

import app.mobiling.client.contract.message.thread.MessageItemPayload
import app.mobiling.client.data.message.thread.MessageThreadGateway

/**
 * Marketing America Corp. Oleksandr Tishchenko
 */
class ListMessageItemsUseCase(
    private val gateway: MessageThreadGateway,
) {
    suspend operator fun invoke(threadId: String): List<MessageItemPayload> = gateway.listItems(threadId)
}
