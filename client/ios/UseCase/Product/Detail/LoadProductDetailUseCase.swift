import Foundation

// Marketing America Corp. Oleksandr Tishchenko
public struct LoadProductDetailUseCase: Sendable {
    private let gateway: any ProductDetailGateway

    public init(gateway: any ProductDetailGateway) {
        self.gateway = gateway
    }

    public func callAsFunction(productId: String) async throws -> ProductDetailPayload? {
        try await gateway.loadProductDetail(productId: productId)
    }
}
