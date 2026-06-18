import Foundation

// Marketing America Corp. Oleksandr Tishchenko
public struct CatalogNodeSummary: Sendable, Equatable {
    public let nodeId: String
    public let parentNodeId: String?
    public let title: String
    public let slug: String?
    public let childCount: Int
    public let productCount: Int?
}
