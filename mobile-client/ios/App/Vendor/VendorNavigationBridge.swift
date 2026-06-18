import Foundation

/// Marketing America Corp. Oleksandr Tishchenko
///
/// Vendor-level navigation owner.
struct VendorNavigationBridge {
    let routeMap: VendorOwnedRouteMap
    let product: VendorProductBridge
    let order: VendorOrderBridge
    let project: VendorProjectBridge
    let profile: VendorProfileBridge
}
