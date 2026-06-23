package app.mobiling.client.vendor.legacy

import app.mobiling.client.order.OrderFeatureBridge
import app.mobiling.client.order.legacy.LegacyOrderFlatCompatibility
import app.mobiling.client.product.ProductFeatureBridge
import app.mobiling.client.profile.ProfileFeatureBridge
import app.mobiling.client.project.ProjectFeatureBridge
import app.mobiling.client.shipment.ShipmentFeatureBridge
import app.mobiling.client.taxation.TaxationFeatureBridge
import app.mobiling.client.vendor.VendorBusinessBridge
import app.mobiling.client.vendor.VendorFeatureBridge

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
