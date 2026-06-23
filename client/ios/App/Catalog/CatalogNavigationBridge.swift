import Foundation

// Marketing America Corp. Oleksandr Tishchenko
// Catalog-level navigation owner.
public struct CatalogNavigationBridge: Sendable {
    public let feature: CatalogFeatureBridge
    public let routeMap: CatalogCommerceRouteMap
    public let vendor: VendorNavigationBridge
    public let product: VendorProductBridge

    public init(
        feature: CatalogFeatureBridge,
        routeMap: CatalogCommerceRouteMap,
        vendor: VendorNavigationBridge,
        product: VendorProductBridge
    ) {
        self.feature = feature
        self.routeMap = routeMap
        self.vendor = vendor
        self.product = product
    }
}
