package com.smartresponsor.mobile.client.usecase.message.thread

import com.smartresponsor.mobile.client.contract.message.thread.MessageItemPayload
import com.smartresponsor.mobile.client.data.message.thread.MessageThreadGateway

/**
 * Marketing America Corp. Oleksandr Tishchenko
 */
class ListMessageItemsUseCase(
    private val gateway: MessageThreadGateway,
) {
    suspend operator fun invoke(threadId: String): List<MessageItemPayload> = gateway.listItems(threadId)
}
