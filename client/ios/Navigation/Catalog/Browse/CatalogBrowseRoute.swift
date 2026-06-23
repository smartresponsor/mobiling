import Foundation

// Marketing America Corp. Oleksandr Tishchenko
public struct CatalogBrowseRoute: Sendable, Equatable {
    public let parentNodeId: String?
    public let searchText: String?

    public init(parentNodeId: String? = nil, searchText: String? = nil) {
        self.parentNodeId = parentNodeId
        self.searchText = searchText
    }
}
