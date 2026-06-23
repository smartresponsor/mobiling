package app.mobiling.client.usecase.message.thread

import app.mobiling.client.contract.message.thread.MessageThreadSummary
import app.mobiling.client.data.message.thread.MessageThreadGateway

/**
 * Marketing America Corp. Oleksandr Tishchenko
 */
class ListMessageThreadsUseCase(
    private val gateway: MessageThreadGateway,
) {
    suspend operator fun invoke(): List<MessageThreadSummary> = gateway.listThreads()
}
