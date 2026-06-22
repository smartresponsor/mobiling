package app.mobiling.client.client.usecase.message.thread

import app.mobiling.client.client.contract.message.thread.MessageItemPayload
import app.mobiling.client.client.contract.message.thread.SendMessageRequest
import app.mobiling.client.client.data.message.thread.MessageThreadGateway

/**
 * Marketing America Corp. Oleksandr Tishchenko
 */
class SendMessageUseCase(
    private val gateway: MessageThreadGateway,
) {
    suspend operator fun invoke(request: SendMessageRequest): MessageItemPayload = gateway.sendMessage(request)
}
