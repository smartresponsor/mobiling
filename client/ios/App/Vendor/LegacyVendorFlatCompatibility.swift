import Foundation

/// Marketing America Corp. Oleksandr Tishchenko
///
/// Dedicated flat compatibility shim for vendor-owned business flows.
///
/// This file is the only vendor-level place where flat Order / Shipment /
/// Taxation compatibility is assembled before handing off into wrapper-first
/// vendor business composition.
enum LegacyVendorFlatCompatibility {
    static func fromFlatFeatures(
        vendor: VendorFeatureBridge,
        productFeature: ProductFeatureBridge,
        orderFeature: OrderFeatureBridge,
        projectFeature: ProjectFeatureBridge,
        profileFeature: ProfileFeatureBridge,
        shipmentFeature: ShipmentFeatureBridge,
        taxationFeature: TaxationFeatureBridge
    ) -> VendorBusinessBridge {
        LegacyVendorBusinessBridgeFactory.fromVendorFlatAndOrderBusiness(
            vendor: vendor,
            productFeature: productFeature,
            orderBusiness: LegacyOrderFlatCompatibility.fromFlatFeatures(
                order: orderFeature,
                shipment: shipmentFeature,
                taxation: taxationFeature
            ),
            projectFeature: projectFeature,
            profileFeature: profileFeature
        )
    }
}
