package com.smartresponsor.mobile.app.vendor.legacy

import com.smartresponsor.mobile.app.order.OrderFeatureBridge
import com.smartresponsor.mobile.app.order.legacy.LegacyOrderFlatCompatibility
import com.smartresponsor.mobile.app.product.ProductFeatureBridge
import com.smartresponsor.mobile.app.profile.ProfileFeatureBridge
import com.smartresponsor.mobile.app.project.ProjectFeatureBridge
import com.smartresponsor.mobile.app.shipment.ShipmentFeatureBridge
import com.smartresponsor.mobile.app.taxation.TaxationFeatureBridge
import com.smartresponsor.mobile.app.vendor.VendorBusinessBridge
import com.smartresponsor.mobile.app.vendor.VendorFeatureBridge

/**
 * Marketing America Corp. Oleksandr Tishchenko
 *
 * Dedicated flat compatibility shim for vendor-owned business flows.
 *
 * This file is the only vendor-level place where flat Order / Shipment /
 * Taxation compatibility is assembled before handing off into wrapper-first
 * vendor business composition.
 */
object LegacyVendorFlatCompatibility {
    fun fromFlatFeatures(
        vendor: VendorFeatureBridge,
        product: ProductFeatureBridge,
        order: OrderFeatureBridge,
        project: ProjectFeatureBridge,
        profile: ProfileFeatureBridge,
        shipment: ShipmentFeatureBridge,
        taxation: TaxationFeatureBridge,
    ): VendorBusinessBridge = LegacyVendorBusinessBridgeFactory.fromVendorFlatAndOrderBusiness(
        vendor = vendor,
        product = product,
        order = LegacyOrderFlatCompatibility.fromFlatFeatures(
            order = order,
            shipment = shipment,
            taxation = taxation,
        ),
        project = project,
        profile = profile,
    )
}
