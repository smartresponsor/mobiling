import Foundation

// Marketing America Corp. Oleksandr Tishchenko
public struct ListCatalogNodesQuery: Sendable, Equatable {
    public let parentNodeId: String?
    public let searchText: String?
    public let includeEmptyNodes: Bool
}
