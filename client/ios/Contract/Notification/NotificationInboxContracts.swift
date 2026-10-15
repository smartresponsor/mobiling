import Foundation

public struct NotificationInboxItem: Sendable, Codable, Identifiable {
    public let id: String
    public let notificationId: String
    public let status: String
    public let sourceComponent: String
    public let eventName: String
    public let topic: String
    public let title: String
    public let body: String
    public let priority: String
    public let actionUrl: String?
    public let createdAt: String
    public let readAt: String?
    public let needCategory: String?
    public let needConfidence: Double?
    public let needLocale: String?
    public let needClassifierVersion: String?

    public init(
        id: String,
        notificationId: String,
        status: String,
        sourceComponent: String = "unknown",
        eventName: String = "unknown",
        topic: String = "default",
        title: String,
        body: String,
        priority: String,
        actionUrl: String?,
        createdAt: String,
        readAt: String?,
        needCategory: String? = nil,
        needConfidence: Double? = nil,
        needLocale: String? = nil,
        needClassifierVersion: String? = nil
    ) {
        self.id = id
        self.notificationId = notificationId
        self.status = status
        self.sourceComponent = sourceComponent
        self.eventName = eventName
        self.topic = topic
        self.title = title
        self.body = body
        self.priority = priority
        self.actionUrl = actionUrl
        self.createdAt = createdAt
        self.readAt = readAt
        self.needCategory = needCategory
        self.needConfidence = needConfidence
        self.needLocale = needLocale
        self.needClassifierVersion = needClassifierVersion
    }
}

public struct NotificationInboxPayload: Sendable, Codable {
    public let items: [NotificationInboxItem]
    public let unreadCount: Int
}

public struct RecognizedNeedRequest: Sendable, Codable {
    public let summary: String
    public let category: String
    public let confidence: Double?
    public let source: String
    public let locale: String?
    public let classifierVersion: String?
    public let correlationId: String?

    public init(summary: String, category: String, confidence: Double?, source: String = "voice", locale: String? = nil, classifierVersion: String? = nil, correlationId: String? = nil) {
        self.summary = summary
        self.category = category
        self.confidence = confidence
        self.source = source
        self.locale = locale
        self.classifierVersion = classifierVersion
        self.correlationId = correlationId
    }
}

public struct NotificationSubscriptionRequest: Sendable, Codable {
    public let token: String
    public let platform: String
    public let appKey: String
    public let deviceId: String
    public let enabled: Bool

    public init(token: String, platform: String, appKey: String, deviceId: String, enabled: Bool = true) {
        self.token = token
        self.platform = platform
        self.appKey = appKey
        self.deviceId = deviceId
        self.enabled = enabled
    }
}
