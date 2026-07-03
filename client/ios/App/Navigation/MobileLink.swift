import Foundation

// Marketing America Corp. Oleksandr Tishchenko
public struct MobileLink: Sendable, Equatable {
    public let raw: String
    public let route: String
    public let query: [String: String]
}
