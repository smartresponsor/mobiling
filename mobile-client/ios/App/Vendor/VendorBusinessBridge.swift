import Foundation

/// Marketing America Corp. Oleksandr Tishchenko
///
/// Vendor-centric business bridge.
///
/// Active surface is wrapper-first: Vendor owns Product / Order / Project /
/// Profile through vendor-owned wrappers only.
///
/// Legacy flat-feature compatibility moved to LegacyVendorBusinessBridgeFactory
/// so the active bridge no longer depends on flat Shipment/Taxation feature bridges.
struct VendorBusinessBridge {
    let vendor: VendorFeatureBridge
    let product: VendorProductBridge
    let order: VendorOrderBridge
    let project: VendorProjectBridge
    let profile: VendorProfileBridge

    init(
        vendor: VendorFeatureBridge,
        product: VendorProductBridge,
        order: VendorOrderBridge,
        project: VendorProjectBridge,
        profile: VendorProfileBridge
    ) {
        self.vendor = vendor
        self.product = product
        self.order = order
        self.project = project
        self.profile = profile
    }

    func ownedRouteMap() -> VendorOwnedRouteMap {
        VendorOwnedRouteMap()
    }

    func navigationBridge() -> VendorNavigationBridge {
        VendorNavigationBridge(
            routeMap: ownedRouteMap(),
            product: product,
            order: order,
            project: project,
            profile: profile
        )
    }
}
