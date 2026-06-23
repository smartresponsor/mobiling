package app.mobiling.client.data.message.thread

import app.mobiling.client.contract.message.thread.MessageItemPayload
import app.mobiling.client.contract.message.thread.MessageThreadSummary
import app.mobiling.client.contract.message.thread.SendMessageRequest

/**
 * Marketing America Corp. Oleksandr Tishchenko
 */
interface MessageThreadGateway {
    suspend fun listThreads(): List<MessageThreadSummary>
    suspend fun listItems(threadId: String): List<MessageItemPayload>
    suspend fun sendMessage(request: SendMessageRequest): MessageItemPayload
}
