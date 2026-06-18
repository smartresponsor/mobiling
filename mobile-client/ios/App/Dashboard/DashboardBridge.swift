import Foundation

// Marketing America Corp. Oleksandr Tishchenko
// Dashboard composition bridge.
public struct DashboardBridge: Sendable {
    public let catalog: CatalogFeatureBridge
    public let message: MessageFeatureBridge
    public let vendor: VendorBusinessBridge
    public let auth: AuthFeatureBridge

    public init(
        catalog: CatalogFeatureBridge,
        message: MessageFeatureBridge,
        vendor: VendorBusinessBridge,
        auth: AuthFeatureBridge
    ) {
        self.catalog = catalog
        self.message = message
        self.vendor = vendor
        self.auth = auth
    }
}
