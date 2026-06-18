import Foundation

/// Marketing America Corp. Oleksandr Tishchenko
///
/// App-level bridge for product-domain controlled rewire.
@available(*, deprecated, message: "Prefer VendorBusinessBridge.ownedProduct() or VendorNavigationBridge.product for vendor-owned navigation.")
struct ProductFeatureBridge {
    let listingGateway: ProductListingGateway
    let detailGateway: ProductDetailGateway

    func list(query: ProductListQuery) async -> [ProductCardSummary] {
        await ListProductsUseCase(gateway: listingGateway).invoke(query: query)
    }

    func detail(productId: String) async -> ProductDetailPayload? {
        await LoadProductDetailUseCase(gateway: detailGateway).invoke(productId: productId)
    }
}
