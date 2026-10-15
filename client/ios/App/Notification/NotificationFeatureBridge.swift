import Foundation

public struct NotificationFeatureBridge: Sendable {
    private let gateway: NotificationGateway

    public init(gateway: NotificationGateway) { self.gateway = gateway }
    public func inbox() async throws -> NotificationInboxPayload { try await gateway.inbox() }
    public func unreadCount() async throws -> Int { try await gateway.unreadCount() }
    public func markRead(ids: [String]) async throws -> Int { try await gateway.markRead(ids: ids) }
    public func subscription(request: NotificationSubscriptionRequest) async throws -> Bool { try await gateway.subscription(request: request) }
}
