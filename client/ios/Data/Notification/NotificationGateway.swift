import Foundation

public protocol NotificationGateway: Sendable {
    func inbox() async throws -> NotificationInboxPayload
    func unreadCount() async throws -> Int
    func markRead(ids: [String]) async throws -> Int
    func recognizedNeed(request: RecognizedNeedRequest) async throws -> Bool
    func requestNeedCallback(recipientEntryId: String) async throws -> Bool
    func subscription(request: NotificationSubscriptionRequest) async throws -> Bool
}
