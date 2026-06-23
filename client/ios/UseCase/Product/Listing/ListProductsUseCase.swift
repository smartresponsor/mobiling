import Foundation

// Marketing America Corp. Oleksandr Tishchenko
public struct ListProductsUseCase: Sendable {
    private let gateway: any ProductListingGateway

    public init(gateway: any ProductListingGateway) {
        self.gateway = gateway
    }

    public func callAsFunction(query: ProductListQuery) async throws -> [ProductCardSummary] {
        try await gateway.listProducts(query: query)
    }
}
