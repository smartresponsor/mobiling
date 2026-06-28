package app.mobiling.client.dashboard

import app.mobiling.client.auth.AccessAuthFeatureBridge
import app.mobiling.client.catalog.CatalogNavigationBridge
import app.mobiling.client.message.MessageFeatureBridge
import app.mobiling.client.vendor.VendorBusinessBridge

/**
 * Marketing America Corp. Oleksandr Tishchenko
 *
 * Dashboard navigation bridge.
 *
 * It exposes the canonical root entry sections and preserves ownership of
 * secondary flows through Catalog -> Vendor/Product and Vendor -> Order
 * ownership chains rather than elevating commerce subflows to flat
 * Dashboard entries.
 */
class DashboardNavigationBridge(
    private val routeMap: DashboardRouteMap,
    private val catalog: CatalogNavigationBridge,
    private val message: MessageFeatureBridge,
    private val vendor: VendorBusinessBridge,
    private val auth: AccessAuthFeatureBridge,
) {
    fun routeMap(): DashboardRouteMap = routeMap

    fun catalog(): CatalogNavigationBridge = catalog

    fun message(): MessageFeatureBridge = message

    fun vendor(): VendorBusinessBridge = vendor

    fun auth(): AccessAuthFeatureBridge = auth
}
