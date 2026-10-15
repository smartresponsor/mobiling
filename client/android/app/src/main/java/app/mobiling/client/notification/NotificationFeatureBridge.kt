package app.mobiling.client.notification

import app.mobiling.client.contract.notification.NotificationInboxPayload
import app.mobiling.client.contract.notification.NotificationSubscriptionRequest
import app.mobiling.client.data.notification.NotificationGateway

class NotificationFeatureBridge(
    private val gateway: NotificationGateway,
) {
    suspend fun inbox(): NotificationInboxPayload = gateway.inbox()
    suspend fun unreadCount(): Int = gateway.unreadCount()
    suspend fun markRead(ids: List<String>): Int = gateway.markRead(ids)
    suspend fun subscription(request: NotificationSubscriptionRequest): Boolean = gateway.subscription(request)
}
