package app.mobiling.client.data.notification

import app.mobiling.client.contract.notification.NotificationInboxPayload
import app.mobiling.client.contract.notification.NotificationSubscriptionRequest

interface NotificationGateway {
    suspend fun inbox(): NotificationInboxPayload
    suspend fun unreadCount(): Int
    suspend fun markRead(ids: List<String>): Int
    suspend fun subscription(request: NotificationSubscriptionRequest): Boolean
}
