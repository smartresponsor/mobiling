package com.smartresponsor.mobile.client.contract.message.thread

/**
 * Marketing America Corp. Oleksandr Tishchenko
 */
data class MessageItemPayload(
    val messageId: String,
    val threadId: String,
    val body: String,
    val senderId: String,
    val sentAtIso8601: String,
)
