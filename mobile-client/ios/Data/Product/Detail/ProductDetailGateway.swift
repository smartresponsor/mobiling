import Foundation

// Marketing America Corp. Oleksandr Tishchenko
public protocol ProductDetailGateway: Sendable {
    func loadProductDetail(productId: String) async throws -> ProductDetailPayload?
}
