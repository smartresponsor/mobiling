package com.smartresponsor.mobile.client.contract.message.thread

/**
 * Marketing America Corp. Oleksandr Tishchenko
 */
data class MessageThreadSummary(
    val threadId: String,
    val subject: String?,
    val lastMessagePreview: String,
    val unreadCount: Int,
    val updatedAtIso8601: String,
)
