import Foundation

// Marketing America Corp. Oleksandr Tishchenko
public struct LoadCatalogNodeDetailUseCase {
    public let gateway: CatalogNodeDetailGateway

    public init(gateway: CatalogNodeDetailGateway) {
        self.gateway = gateway
    }

    public func callAsFunction(nodeId: String) async throws -> CatalogNodeDetailPayload {
        try await gateway.loadNodeDetail(nodeId: nodeId)
    }
}
