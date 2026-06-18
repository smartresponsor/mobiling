package com.smartresponsor.mobile.client.data.message.thread

import com.smartresponsor.mobile.client.contract.message.thread.MessageItemPayload
import com.smartresponsor.mobile.client.contract.message.thread.MessageThreadSummary
import com.smartresponsor.mobile.client.contract.message.thread.SendMessageRequest

/**
 * Marketing America Corp. Oleksandr Tishchenko
 */
interface MessageThreadGateway {
    suspend fun listThreads(): List<MessageThreadSummary>
    suspend fun listItems(threadId: String): List<MessageItemPayload>
    suspend fun sendMessage(request: SendMessageRequest): MessageItemPayload
}
