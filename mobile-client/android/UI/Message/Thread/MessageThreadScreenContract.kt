package com.smartresponsor.mobile.client.ui.message.thread

import com.smartresponsor.mobile.client.contract.message.thread.MessageItemPayload
import com.smartresponsor.mobile.client.contract.message.thread.MessageThreadSummary

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
