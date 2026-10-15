package app.mobiling.client.contract.notification

data class NotificationInboxItem(
    val id: String,
    val notificationId: String,
    val status: String,
    val sourceComponent: String,
    val eventName: String,
    val topic: String,
    val title: String,
    val body: String,
    val priority: String,
    val actionUrl: String?,
    val createdAt: String,
    val readAt: String?,
    val needCategory: String? = null,
    val needConfidence: Double? = null,
    val needLocale: String? = null,
    val needClassifierVersion: String? = null,
)

data class NotificationInboxPayload(
    val items: List<NotificationInboxItem>,
    val unreadCount: Int,
)

data class RecognizedNeedRequest(
    val summary: String,
    val category: String,
    val confidence: Double?,
    val source: String = "voice",
    val locale: String? = null,
    val classifierVersion: String? = null,
    val correlationId: String? = null,
)

data class NotificationSubscriptionRequest(
    val token: String,
    val platform: String,
    val appKey: String,
    val deviceId: String,
    val enabled: Boolean = true,
)
