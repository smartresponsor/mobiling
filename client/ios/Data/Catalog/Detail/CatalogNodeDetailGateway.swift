import Foundation

// Marketing America Corp. Oleksandr Tishchenko
public protocol CatalogNodeDetailGateway {
    func loadNodeDetail(nodeId: String) async throws -> CatalogNodeDetailPayload
}
