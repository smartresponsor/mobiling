package app.mobiling.client.contract.notification

data class NotificationInboxItem(
    val id: String,
    val notificationId: String,
    val status: String,
    val title: String,
    val body: String,
    val priority: String,
    val actionUrl: String?,
    val createdAt: String,
    val readAt: String?,
)

data class NotificationInboxPayload(
    val items: List<NotificationInboxItem>,
    val unreadCount: Int,
)

data class NotificationSubscriptionRequest(
    val token: String,
    val platform: String,
    val appKey: String,
    val deviceId: String,
    val enabled: Boolean = true,
)
