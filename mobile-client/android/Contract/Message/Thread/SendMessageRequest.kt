package app.mobiling.client.client.contract.message.thread

/**
 * Marketing America Corp. Oleksandr Tishchenko
 */
data class SendMessageRequest(
    val threadId: String,
    val body: String,
)
