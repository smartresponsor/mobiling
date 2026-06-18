package com.smartresponsor.mobile.app.dashboard

import com.smartresponsor.mobile.app.auth.AuthFeatureBridge
import com.smartresponsor.mobile.app.catalog.CatalogNavigationBridge
import com.smartresponsor.mobile.app.message.MessageFeatureBridge
import com.smartresponsor.mobile.app.vendor.VendorBusinessBridge

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
    private val auth: AuthFeatureBridge,
) {
    fun routeMap(): DashboardRouteMap = routeMap

    fun catalog(): CatalogNavigationBridge = catalog

    fun message(): MessageFeatureBridge = message

    fun vendor(): VendorBusinessBridge = vendor

    fun auth(): AuthFeatureBridge = auth
}
