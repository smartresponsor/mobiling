package com.smartresponsor.mobile.app.message

import com.smartresponsor.mobile.client.contract.message.thread.MessageItemPayload
import com.smartresponsor.mobile.client.contract.message.thread.MessageThreadSummary
import com.smartresponsor.mobile.client.contract.message.thread.SendMessageRequest
import com.smartresponsor.mobile.client.data.message.thread.MessageThreadGateway
import com.smartresponsor.mobile.client.usecase.message.thread.ListMessageItemsUseCase
import com.smartresponsor.mobile.client.usecase.message.thread.ListMessageThreadsUseCase
import com.smartresponsor.mobile.client.usecase.message.thread.SendMessageUseCase

/**
 * Marketing America Corp. Oleksandr Tishchenko
 *
 * App-level bridge for the first business-domain controlled rewire.
 * It preserves a stable feature entry point while delegating behavior
 * into canonical Contract/Data/UseCase slices.
 */
class MessageFeatureBridge(
    private val gateway: MessageThreadGateway,
) {
    suspend fun listThreads(): List<MessageThreadSummary> =
        ListMessageThreadsUseCase(gateway).invoke()

    suspend fun listItems(threadId: String): List<MessageItemPayload> =
        ListMessageItemsUseCase(gateway).invoke(threadId)

    suspend fun send(request: SendMessageRequest): MessageItemPayload =
        SendMessageUseCase(gateway).invoke(request)
}
