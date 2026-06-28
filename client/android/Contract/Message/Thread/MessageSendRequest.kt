package app.mobiling.client.contract.message.thread

/**
 * Marketing America Corp. Oleksandr Tishchenko
 */
data class MessageSendRequest(
    val threadId: String,
    val body: String,
)
