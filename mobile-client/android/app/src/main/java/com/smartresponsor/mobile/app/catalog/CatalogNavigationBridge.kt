package com.smartresponsor.mobile.app.catalog

import com.smartresponsor.mobile.app.vendor.VendorNavigationBridge
import com.smartresponsor.mobile.app.vendor.VendorProductBridge

/**
 * Marketing America Corp. Oleksandr Tishchenko
 *
 * Catalog-level navigation owner.
 *
 * Catalog remains a primary Dashboard section while routing commercial
 * discovery into Vendor-owned commerce flows.
 */
class CatalogNavigationBridge(
    private val feature: CatalogFeatureBridge,
    private val routeMap: CatalogCommerceRouteMap,
    private val vendor: VendorNavigationBridge,
    private val product: VendorProductBridge,
) {
    fun feature(): CatalogFeatureBridge = feature

    fun routeMap(): CatalogCommerceRouteMap = routeMap

    fun vendor(): VendorNavigationBridge = vendor

    fun product(): VendorProductBridge = product
}
