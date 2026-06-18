import Foundation

// Marketing America Corp. Oleksandr Tishchenko
public struct MessageItemPayload: Sendable, Codable {
    public let messageId: String
    public let threadId: String
    public let body: String
    public let senderId: String
    public let sentAtIso8601: String
}
