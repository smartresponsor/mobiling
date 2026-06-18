package com.smartresponsor.mobile.client.usecase.message.thread

import com.smartresponsor.mobile.client.contract.message.thread.messagethreadsummary
import com.smartresponsor.mobile.client.data.message.thread.messagethreadgateway

/**
 * Marketing America Corp. Oleksandr Tishchenko
 */
class ListMessageThreadsUseCase(
    private val gateway: MessageThreadGateway,
) {
    suspend operator fun invoke(): List<MessageThreadSummary> = gateway.listThreads()
}
