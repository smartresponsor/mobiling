package app.mobiling.client.message

import app.mobiling.client.contract.message.thread.MessageItemPayload
import app.mobiling.client.contract.message.thread.MessageThreadSummary
import app.mobiling.client.contract.message.thread.MessageSendRequest
import app.mobiling.client.data.message.thread.MessageThreadGateway
import app.mobiling.client.usecase.message.thread.MessageListItemUseCase
import app.mobiling.client.usecase.message.thread.MessageListThreadUseCase
import app.mobiling.client.usecase.message.thread.MessageSendUseCase

/**
 * Marketing America Corp. Oleksandr Tishchenko
 *
 * App-level bridge for the first business-domain controlled rewire.
 * It preserves a stable feature entry point while delegating behavior
 * into canonical Contract/Data/UseCase slices.
 */
class MessageFeatureBridge(
    private val gateway: MessageThreadGateway,
) {
    suspend fun listThreads(): List<MessageThreadSummary> =
        MessageListThreadUseCase(gateway).invoke()

    suspend fun listItems(threadId: String): List<MessageItemPayload> =
        MessageListItemUseCase(gateway).invoke(threadId)

    suspend fun send(request: MessageSendRequest): MessageItemPayload =
        MessageSendUseCase(gateway).invoke(request)

    suspend fun markRead(threadId: String, userId: String, messageId: String) =
        gateway.markRead(threadId, userId, messageId)
}
