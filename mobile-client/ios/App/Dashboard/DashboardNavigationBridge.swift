import Foundation

// Marketing America Corp. Oleksandr Tishchenko
// Dashboard navigation bridge.
public struct DashboardNavigationBridge: Sendable {
    public let routeMap: DashboardRouteMap
    public let catalog: CatalogNavigationBridge
    public let message: MessageFeatureBridge
    public let vendor: VendorBusinessBridge
    public let auth: AuthFeatureBridge

    public init(
        routeMap: DashboardRouteMap,
        catalog: CatalogNavigationBridge,
        message: MessageFeatureBridge,
        vendor: VendorBusinessBridge,
        auth: AuthFeatureBridge
    ) {
        self.routeMap = routeMap
        self.catalog = catalog
        self.message = message
        self.vendor = vendor
        self.auth = auth
    }
}
