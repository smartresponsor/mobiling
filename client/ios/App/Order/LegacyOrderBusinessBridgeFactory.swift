import Foundation

/// Marketing America Corp. Oleksandr Tishchenko
///
/// Transitional order compatibility boundary.
///
/// Active compatibility prefers owned wrappers. Flat Shipment/Taxation
/// compatibility is isolated behind LegacyOrderFlatCompatibility so this
/// factory can converge on wrapper-first construction.
enum LegacyOrderBusinessBridgeFactory {
    static func fromOwnedWrappers(
        order: OrderFeatureBridge,
        shipment: OrderShipmentBridge,
        taxation: OrderTaxationBridge
    ) -> OrderBusinessBridge {
        OrderBusinessBridge(
            order: order,
            shipment: shipment,
            taxation: taxation
        )
    }

    @available(*, deprecated, message: "Prefer LegacyOrderFlatCompatibility.fromFlatFeatures(...) or fromOwnedWrappers(...).")
    static func fromFlatFeatures(
        order: OrderFeatureBridge,
        shipment: ShipmentFeatureBridge,
        taxation: TaxationFeatureBridge
    ) -> OrderBusinessBridge {
        LegacyOrderFlatCompatibility.fromFlatFeatures(
            order: order,
            shipment: shipment,
            taxation: taxation
        )
    }
}
