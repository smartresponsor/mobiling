import Foundation

// Marketing America Corp. Oleksandr Tishchenko
public struct ProductCardSummary: Sendable, Equatable {
    public let productId: String
    public let title: String
    public let subtitle: String?
    public let priceLabel: String?
    public let thumbnailUrl: String?
    public let availabilityLabel: String?
}
