import Foundation

// Marketing America Corp. Oleksandr Tishchenko
public struct SendMessageRequest: Sendable, Codable {
    public let threadId: String
    public let body: String
}
