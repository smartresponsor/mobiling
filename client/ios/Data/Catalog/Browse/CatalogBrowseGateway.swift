import Foundation

// Marketing America Corp. Oleksandr Tishchenko
public protocol CatalogBrowseGateway {
    func listNodes(query: ListCatalogNodesQuery) async throws -> [CatalogNodeSummary]
}
