import Foundation

// Marketing America Corp. Oleksandr Tishchenko
// App-level bridge for catalog-domain controlled rewire.
public struct CatalogFeatureBridge: Sendable {
    private let browseGateway: any CatalogBrowseGateway
    private let detailGateway: any CatalogNodeDetailGateway

    public init(
        browseGateway: any CatalogBrowseGateway,
        detailGateway: any CatalogNodeDetailGateway
    ) {
        self.browseGateway = browseGateway
        self.detailGateway = detailGateway
    }

    public func list(query: ListCatalogNodesQuery) async throws -> [CatalogNodeSummary] {
        try await ListCatalogNodesUseCase(gateway: browseGateway)(query: query)
    }

    public func detail(nodeId: String) async throws -> CatalogNodeDetailPayload? {
        try await LoadCatalogNodeDetailUseCase(gateway: detailGateway)(nodeId: nodeId)
    }
}
