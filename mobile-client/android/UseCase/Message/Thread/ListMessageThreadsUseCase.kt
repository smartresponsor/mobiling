package com.smartresponsor.mobile.client.usecase.message.thread

import com.smartresponsor.mobile.client.contract.message.thread.MessageThreadSummary
import com.smartresponsor.mobile.client.data.message.thread.MessageThreadGateway

/**
 * Marketing America Corp. Oleksandr Tishchenko
 */
class ListMessageThreadsUseCase(
    private val gateway: MessageThreadGateway,
) {
    suspend operator fun invoke(): List<MessageThreadSummary> = gateway.listThreads()
}
