import Foundation

/// Marketing America Corp. Oleksandr Tishchenko
///
/// Transitional vendor compatibility boundary.
///
/// Active compatibility now prefers vendor + OrderBusinessBridge handoff.
/// Flat compatibility is isolated behind LegacyVendorFlatCompatibility.
enum LegacyVendorBusinessBridgeFactory {
    static func fromVendorFlatAndOrderBusiness(
        vendor: VendorFeatureBridge,
        productFeature: ProductFeatureBridge,
        orderBusiness: OrderBusinessBridge,
        projectFeature: ProjectFeatureBridge,
        profileFeature: ProfileFeatureBridge
    ) -> VendorBusinessBridge {
        VendorBusinessBridge(
            vendor: vendor,
            product: VendorProductBridge(feature: productFeature),
            order: VendorOrderBridge(business: orderBusiness),
            project: VendorProjectBridge(feature: projectFeature),
            profile: VendorProfileBridge(feature: profileFeature)
        )
    }

    @available(*, deprecated, message: "Prefer LegacyVendorFlatCompatibility.fromFlatFeatures(...) or fromVendorFlatAndOrderBusiness(...).")
    static func fromFlatFeatures(
        vendor: VendorFeatureBridge,
        productFeature: ProductFeatureBridge,
        orderFeature: OrderFeatureBridge,
        projectFeature: ProjectFeatureBridge,
        profileFeature: ProfileFeatureBridge,
        shipmentFeature: ShipmentFeatureBridge,
        taxationFeature: TaxationFeatureBridge
    ) -> VendorBusinessBridge {
        LegacyVendorFlatCompatibility.fromFlatFeatures(
            vendor: vendor,
            productFeature: productFeature,
            orderFeature: orderFeature,
            projectFeature: projectFeature,
            profileFeature: profileFeature,
            shipmentFeature: shipmentFeature,
            taxationFeature: taxationFeature
        )
    }
}
