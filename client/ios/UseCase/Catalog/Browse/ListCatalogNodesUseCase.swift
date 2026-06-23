import Foundation

// Marketing America Corp. Oleksandr Tishchenko
public struct ListCatalogNodesUseCase {
    public let gateway: CatalogBrowseGateway

    public init(gateway: CatalogBrowseGateway) {
        self.gateway = gateway
    }

    public func callAsFunction(query: ListCatalogNodesQuery) async throws -> [CatalogNodeSummary] {
        try await gateway.listNodes(query: query)
    }
}
