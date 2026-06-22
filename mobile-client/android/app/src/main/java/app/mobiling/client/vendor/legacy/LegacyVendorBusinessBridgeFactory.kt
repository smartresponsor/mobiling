package app.mobiling.client.vendor.legacy

import app.mobiling.client.order.OrderBusinessBridge
import app.mobiling.client.product.ProductFeatureBridge
import app.mobiling.client.profile.ProfileFeatureBridge
import app.mobiling.client.project.ProjectFeatureBridge
import app.mobiling.client.vendor.VendorBusinessBridge
import app.mobiling.client.vendor.VendorFeatureBridge
import app.mobiling.client.vendor.VendorOrderBridge
import app.mobiling.client.vendor.VendorProductBridge
import app.mobiling.client.vendor.VendorProfileBridge
import app.mobiling.client.vendor.VendorProjectBridge

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
        order: app.mobiling.client.order.OrderFeatureBridge,
        project: ProjectFeatureBridge,
        profile: ProfileFeatureBridge,
        shipment: app.mobiling.client.shipment.ShipmentFeatureBridge,
        taxation: app.mobiling.client.taxation.TaxationFeatureBridge,
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
