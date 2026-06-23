import Foundation

// Marketing America Corp. Oleksandr Tishchenko
public struct ProductListQuery: Sendable, Equatable {
    public let searchTerm: String?
    public let page: Int
    public let pageSize: Int
}
