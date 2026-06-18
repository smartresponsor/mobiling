import Foundation

// Marketing America Corp. Oleksandr Tishchenko
public struct ProductDetailPayload: Sendable, Equatable {
    public let productId: String
    public let title: String
    public let description: String?
    public let priceLabel: String?
    public let mediaUrls: [String]
    public let vendorLabel: String?
}
