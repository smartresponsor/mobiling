import Foundation

// Marketing America Corp. Oleksandr Tishchenko
public struct MessageThreadSummary: Sendable, Codable {
    public let threadId: String
    public let subject: String?
    public let lastMessagePreview: String
    public let unreadCount: Int
    public let updatedAtIso8601: String
}
