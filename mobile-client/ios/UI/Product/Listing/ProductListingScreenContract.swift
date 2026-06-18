import Foundation

// Marketing America Corp. Oleksandr Tishchenko
public protocol ProductListingScreenContract: AnyObject {
    func render(items: [ProductCardSummary], isLoading: Bool)
    func openDetail(productId: String)
}
