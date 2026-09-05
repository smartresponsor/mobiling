import Foundation

public protocol NotificationGateway: Sendable {
    func inbox() async throws -> NotificationInboxPayload
    func unreadCount() async throws -> Int
    func markRead(ids: [String]) async throws -> Int
    func subscription(request: NotificationSubscriptionRequest) async throws -> Bool
}
