package com.smartresponsor.mobile.app.vendor.legacy

import com.smartresponsor.mobile.app.order.OrderBusinessBridge
import com.smartresponsor.mobile.app.product.ProductFeatureBridge
import com.smartresponsor.mobile.app.profile.ProfileFeatureBridge
import com.smartresponsor.mobile.app.project.ProjectFeatureBridge
import com.smartresponsor.mobile.app.vendor.VendorBusinessBridge
import com.smartresponsor.mobile.app.vendor.VendorFeatureBridge
import com.smartresponsor.mobile.app.vendor.VendorOrderBridge
import com.smartresponsor.mobile.app.vendor.VendorProductBridge
import com.smartresponsor.mobile.app.vendor.VendorProfileBridge
import com.smartresponsor.mobile.app.vendor.VendorProjectBridge

/**
 * Marketing America Corp. Oleksandr Tishchenko
 *
 * Transitional vendor compatibility boundary.
 *
 * Active compatibility now prefers vendor + OrderBusinessBridge handoff.
 * Flat compatibility is isolated behind LegacyVendorFlatCompatibility.
 */
object LegacyVendorBusinessBridgeFactory {
    fun fromVendorFlatAndOrderBusiness(
        vendor: VendorFeatureBridge,
        product: ProductFeatureBridge,
        order: OrderBusinessBridge,
        project: ProjectFeatureBridge,
        profile: ProfileFeatureBridge,
    ): VendorBusinessBridge = VendorBusinessBridge(
        vendor = vendor,
        product = VendorProductBridge(product),
        order = VendorOrderBridge(order),
        project = VendorProjectBridge(project),
        profile = VendorProfileBridge(profile),
    )

    @Deprecated(
        message = "Prefer LegacyVendorFlatCompatibility.fromFlatFeatures(...) or fromVendorFlatAndOrderBusiness(...).",
        replaceWith = ReplaceWith(
            "LegacyVendorFlatCompatibility.fromFlatFeatures(vendor, product, order, project, profile, shipment, taxation)"
        )
    )
    fun fromFlatFeatures(
        vendor: VendorFeatureBridge,
        product: ProductFeatureBridge,
        order: com.smartresponsor.mobile.app.order.OrderFeatureBridge,
        project: ProjectFeatureBridge,
        profile: ProfileFeatureBridge,
        shipment: com.smartresponsor.mobile.app.shipment.ShipmentFeatureBridge,
        taxation: com.smartresponsor.mobile.app.taxation.TaxationFeatureBridge,
    ): VendorBusinessBridge = LegacyVendorFlatCompatibility.fromFlatFeatures(
        vendor = vendor,
        product = product,
        order = order,
        project = project,
        profile = profile,
        shipment = shipment,
        taxation = taxation,
    )
}
