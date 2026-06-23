import Foundation

/// Marketing America Corp. Oleksandr Tishchenko
///
/// Vendor-owned product bridge.
struct VendorProductBridge {
    let feature: ProductFeatureBridge

    func list(query: ProductListQuery) async -> [ProductCardSummary] {
        await feature.list(query: query)
    }

    func detail(productId: String) async -> ProductDetailPayload? {
        await feature.detail(productId: productId)
    }
}
