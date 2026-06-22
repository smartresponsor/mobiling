package app.mobiling.client.ui.message.thread

import app.mobiling.client.contract.message.thread.MessageItemPayload
import app.mobiling.client.contract.message.thread.MessageThreadSummary

/**
 * Marketing America Corp. Oleksandr Tishchenko
 */
data class MessageThreadIndexState(
    val threads: List<MessageThreadSummary> = emptyList(),
)

data class MessageThreadDetailState(
    val threadId: String,
    val items: List<MessageItemPayload> = emptyList(),
)
