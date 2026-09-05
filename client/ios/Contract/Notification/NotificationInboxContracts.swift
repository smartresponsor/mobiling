import Foundation

public struct NotificationInboxItem: Sendable, Codable, Identifiable {
    public let id: String
    public let notificationId: String
    public let status: String
    public let title: String
    public let body: String
    public let priority: String
    public let actionUrl: String?
    public let createdAt: String
    public let readAt: String?
}

public struct NotificationInboxPayload: Sendable, Codable {
    public let items: [NotificationInboxItem]
    public let unreadCount: Int
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
