import Foundation

// Marketing America Corp. Oleksandr Tishchenko
public struct ListCatalogNodesQuery: Sendable, Equatable {
    public let parentNodeId: String?
    public let searchText: String?
    public let includeEmptyNodes: Bool
    public let catalogCode: String?

    public init(parentNodeId: String?, searchText: String?, includeEmptyNodes: Bool, catalogCode: String? = nil) {
        self.parentNodeId = parentNodeId
        self.searchText = searchText
        self.includeEmptyNodes = includeEmptyNodes
        self.catalogCode = catalogCode
    }
}
