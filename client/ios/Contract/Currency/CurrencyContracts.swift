import Foundation

public struct CurrencyMetadataPayload: Codable, Sendable, Identifiable {
    public var id: String { code }
    public let code: String
    public let numericCode: String?
    public let minorUnit: Int
    public let symbol: String?
    public let displayName: String?
}

public struct CurrencyCatalogPayload: Codable, Sendable {
    public let items: [CurrencyMetadataPayload]
}

public struct CurrencyChoicePayload: Codable, Sendable, Identifiable {
    public var id: String { code }
    public let code: String
    public let label: String
    public let symbol: String?
    public let minorUnit: Int
}

public struct CurrencySelectorPayload: Codable, Sendable {
    public let choices: [CurrencyChoicePayload]
    public let selectedCode: String?
    public let placeholder: String
}

