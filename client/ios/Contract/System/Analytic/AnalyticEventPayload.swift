import Foundation

public struct AnalyticEventPayload: Sendable, Equatable {
    public let name: String
    public let attributes: [String: String]
    public let occurredAtIso8601: String?

    public init(name: String, attributes: [String: String] = [:], occurredAtIso8601: String? = nil) {
        self.name = name
        self.attributes = attributes
        self.occurredAtIso8601 = occurredAtIso8601
    }
}
