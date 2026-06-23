package app.mobiling.client.message

import app.mobiling.client.contract.message.thread.MessageItemPayload
import app.mobiling.client.contract.message.thread.MessageThreadSummary
import app.mobiling.client.contract.message.thread.SendMessageRequest
import app.mobiling.client.data.message.thread.MessageThreadGateway
import app.mobiling.client.usecase.message.thread.ListMessageItemsUseCase
import app.mobiling.client.usecase.message.thread.ListMessageThreadsUseCase
import app.mobiling.client.usecase.message.thread.SendMessageUseCase

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
        ListMessageThreadsUseCase(gateway).invoke()

    suspend fun listItems(threadId: String): List<MessageItemPayload> =
        ListMessageItemsUseCase(gateway).invoke(threadId)

    suspend fun send(request: SendMessageRequest): MessageItemPayload =
        SendMessageUseCase(gateway).invoke(request)
}
