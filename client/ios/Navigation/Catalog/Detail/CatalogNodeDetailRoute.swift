import Foundation

// Marketing America Corp. Oleksandr Tishchenko
public struct CatalogNodeDetailRoute: Sendable, Equatable {
    public let nodeId: String

    public init(nodeId: String) {
        self.nodeId = nodeId
    }
}
