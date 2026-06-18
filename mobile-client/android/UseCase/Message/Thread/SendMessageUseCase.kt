package com.smartresponsor.mobile.client.usecase.message.thread

import com.smartresponsor.mobile.client.contract.message.thread.messageitempayload
import com.smartresponsor.mobile.client.contract.message.thread.sendmessagerequest
import com.smartresponsor.mobile.client.data.message.thread.messagethreadgateway

/**
 * Marketing America Corp. Oleksandr Tishchenko
 */
class SendMessageUseCase(
    private val gateway: MessageThreadGateway,
) {
    suspend operator fun invoke(request: SendMessageRequest): MessageItemPayload = gateway.sendMessage(request)
}
