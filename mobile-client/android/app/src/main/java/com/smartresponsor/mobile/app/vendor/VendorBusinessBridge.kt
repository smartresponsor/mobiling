package com.smartresponsor.mobile.app.vendor

/**
 * Marketing America Corp. Oleksandr Tishchenko
 *
 * Vendor-centric business bridge.
 *
 * Active surface is wrapper-first: Vendor owns Product / Order / Project /
 * Profile through vendor-owned wrappers only.
 *
 * Legacy flat-feature compatibility moved to legacy/LegacyVendorBusinessBridgeFactory
 * so the active bridge no longer depends on flat Shipment/Taxation feature bridges.
 */
class VendorBusinessBridge(
    private val vendor: VendorFeatureBridge,
    private val product: VendorProductBridge,
    private val order: VendorOrderBridge,
    private val project: VendorProjectBridge,
    private val profile: VendorProfileBridge,
) {
    fun vendor(): VendorFeatureBridge = vendor

    fun product(): VendorProductBridge = product

    fun order(): VendorOrderBridge = order

    fun project(): VendorProjectBridge = project

    fun profile(): VendorProfileBridge = profile

    fun ownedRouteMap(): VendorOwnedRouteMap = VendorOwnedRouteMap()

    fun navigationBridge(): VendorNavigationBridge = VendorNavigationBridge(
        routeMap = ownedRouteMap(),
        product = product,
        order = order,
        project = project,
        profile = profile,
    )
}
