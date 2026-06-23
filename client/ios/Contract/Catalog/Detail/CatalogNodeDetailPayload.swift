import Foundation

// Marketing America Corp. Oleksandr Tishchenko
public struct CatalogNodeDetailPayload: Sendable, Equatable {
    public let node: CatalogNodeSummary
    public let description: String?
    public let breadcrumbLabels: [String]
    public let featuredProductIds: [String]
}
