package app.mobiling.client.usecase.message.thread

import app.mobiling.client.contract.message.thread.MessageItemPayload
import app.mobiling.client.contract.message.thread.MessageSendRequest
import app.mobiling.client.data.message.thread.MessageThreadGateway

/**
 * Marketing America Corp. Oleksandr Tishchenko
 */
class MessageSendUseCase(
    private val gateway: MessageThreadGateway,
) {
    suspend operator fun invoke(request: MessageSendRequest): MessageItemPayload = gateway.sendMessage(request)
}
