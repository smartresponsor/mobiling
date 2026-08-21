import Foundation

public struct SupportOptionPayload: Identifiable, Sendable {
    public var id: String { value }
    public let label: String
    public let value: String
}

public struct SupportFieldPayload: Identifiable, Sendable {
    public var id: String { name }
    public let name: String
    public let label: String
    public let type: String
    public let value: String?
    public let required: Bool
    public let options: [SupportOptionPayload]
}

public struct SupportActionPayload: Identifiable, Sendable {
    public var id: String { "\(method):\(href):\(label)" }
    public let label: String
    public let href: String
    public let method: String
    public let enabled: Bool
}

public struct SupportRowPayload: Identifiable, Sendable {
    public let id: String
    public let context: String
    public let request: String
    public let description: String
    public let href: String
    public let availableItems: Int
}

public struct CaseRowPayload: Identifiable, Sendable {
    public var id: String { reference }
    public let reference: String
    public let context: String
    public let category: String
    public let status: String
    public let href: String
}

public struct SupportPagePayload: Sendable {
    public let title: String
    public let description: String
    public let rows: [SupportRowPayload]
    public let cases: [CaseRowPayload]
    public let fields: [SupportFieldPayload]
    public let actions: [SupportActionPayload]
    public let action: String?
    public let method: String
    public let reference: String?
    public let status: String?
    public let businessContext: String?
    public let category: String?
    public let descriptionText: String?
}
