import Foundation

// Marketing America Corp. Oleksandr Tishchenko
public protocol ProductListingGateway: Sendable {
    func listProducts(query: ProductListQuery) async throws -> [ProductCardSummary]
}
