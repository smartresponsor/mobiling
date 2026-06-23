import Foundation

// Marketing America Corp. Oleksandr Tishchenko
public struct CatalogBrowseScreenContract: Sendable, Equatable {
    public let title: String
    public let nodes: [CatalogNodeSummary]
    public let isLoading: Bool
}
