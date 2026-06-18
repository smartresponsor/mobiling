package com.smartresponsor.mobile.client.usecase.message.thread

import com.smartresponsor.mobile.client.contract.message.thread.MessageItemPayload
import com.smartresponsor.mobile.client.contract.message.thread.SendMessageRequest
import com.smartresponsor.mobile.client.data.message.thread.MessageThreadGateway

/**
 * Marketing America Corp. Oleksandr Tishchenko
 */
class SendMessageUseCase(
    private val gateway: MessageThreadGateway,
) {
    suspend operator fun invoke(request: SendMessageRequest): MessageItemPayload = gateway.sendMessage(request)
}
