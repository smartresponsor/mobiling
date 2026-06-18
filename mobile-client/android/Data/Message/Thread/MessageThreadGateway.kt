package com.smartresponsor.mobile.client.data.message.thread

import com.smartresponsor.mobile.client.contract.message.thread.messageitempayload
import com.smartresponsor.mobile.client.contract.message.thread.messagethreadsummary
import com.smartresponsor.mobile.client.contract.message.thread.sendmessagerequest

/**
 * Marketing America Corp. Oleksandr Tishchenko
 */
interface MessageThreadGateway {
    suspend fun listThreads(): List<MessageThreadSummary>
    suspend fun listItems(threadId: String): List<MessageItemPayload>
    suspend fun sendMessage(request: SendMessageRequest): MessageItemPayload
}
