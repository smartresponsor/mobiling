package app.mobiling.client.vendor

/**
 * Marketing America Corp. Oleksandr Tishchenko
 *
 * Vendor-level navigation owner.
 *
 * It keeps vendor-owned flows discoverable through a canonical navigation
 * surface rather than as a flat collection of neighboring bridges.
 */
class VendorNavigationBridge(
    private val routeMap: VendorOwnedRouteMap,
    private val product: VendorProductBridge,
    private val order: VendorOrderBridge,
    private val project: VendorProjectBridge,
    private val profile: VendorProfileBridge,
) {
    fun routeMap(): VendorOwnedRouteMap = routeMap

    fun product(): VendorProductBridge = product

    fun order(): VendorOrderBridge = order

    fun project(): VendorProjectBridge = project

    fun profile(): VendorProfileBridge = profile
}
