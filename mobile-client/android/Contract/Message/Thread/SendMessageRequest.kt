package com.smartresponsor.mobile.client.contract.message.thread

/**
 * Marketing America Corp. Oleksandr Tishchenko
 */
data class SendMessageRequest(
    val threadId: String,
    val body: String,
)
