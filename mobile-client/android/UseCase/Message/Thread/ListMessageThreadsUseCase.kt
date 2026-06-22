package app.mobiling.client.client.usecase.message.thread

import app.mobiling.client.client.contract.message.thread.MessageThreadSummary
import app.mobiling.client.client.data.message.thread.MessageThreadGateway

/**
 * Marketing America Corp. Oleksandr Tishchenko
 */
class ListMessageThreadsUseCase(
    private val gateway: MessageThreadGateway,
) {
    suspend operator fun invoke(): List<MessageThreadSummary> = gateway.listThreads()
}
