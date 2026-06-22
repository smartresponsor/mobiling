package app.mobiling.client.vendor

/**
 * Marketing America Corp. Oleksandr Tishchenko
 *
 * Canonical route ownership map for vendor-owned flows.
 */
class VendorOwnedRouteMap {
    fun ownedFlows(): List<VendorOwnedFlow> = listOf(
        VendorOwnedFlow.PRODUCT,
        VendorOwnedFlow.ORDER,
        VendorOwnedFlow.PROJECT,
        VendorOwnedFlow.PROFILE,
    )
}
