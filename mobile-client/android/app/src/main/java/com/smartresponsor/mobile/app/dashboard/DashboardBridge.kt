package com.smartresponsor.mobile.app.dashboard

import com.smartresponsor.mobile.app.auth.AuthFeatureBridge
import com.smartresponsor.mobile.app.catalog.CatalogFeatureBridge
import com.smartresponsor.mobile.app.message.MessageFeatureBridge
import com.smartresponsor.mobile.app.vendor.VendorBusinessBridge

/**
 * Marketing America Corp. Oleksandr Tishchenko
 *
 * Dashboard composition bridge.
 *
 * It reflects the working spec where Dashboard is the only root section
 * and composes the top-level entry surfaces for Catalog, Message,
 * Vendor-centric business, and Auth.
 */
class DashboardBridge(
    private val catalog: CatalogFeatureBridge,
    private val message: MessageFeatureBridge,
    private val vendor: VendorBusinessBridge,
    private val auth: AuthFeatureBridge,
) {
    fun catalog(): CatalogFeatureBridge = catalog

    fun message(): MessageFeatureBridge = message

    fun vendor(): VendorBusinessBridge = vendor

    fun auth(): AuthFeatureBridge = auth
}
