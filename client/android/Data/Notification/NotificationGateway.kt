package app.mobiling.client.data.notification

import app.mobiling.client.contract.notification.NotificationInboxPayload
import app.mobiling.client.contract.notification.NotificationSubscriptionRequest
import app.mobiling.client.contract.notification.RecognizedNeedRequest

interface NotificationGateway {
    suspend fun inbox(): NotificationInboxPayload
    suspend fun unreadCount(): Int
    suspend fun markRead(ids: List<String>): Int
    suspend fun recognizedNeed(request: RecognizedNeedRequest): Boolean
    suspend fun requestNeedCallback(recipientEntryId: String): Boolean
    suspend fun subscription(request: NotificationSubscriptionRequest): Boolean
}
