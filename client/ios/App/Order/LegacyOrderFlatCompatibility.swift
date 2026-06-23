import Foundation

/// Marketing America Corp. Oleksandr Tishchenko
///
/// Dedicated flat compatibility shim for order-owned subflows.
///
/// This file is the only place where flat ShipmentFeatureBridge /
/// TaxationFeatureBridge are adapted into order-owned wrappers for
/// compatibility purposes.
enum LegacyOrderFlatCompatibility {
    static func fromFlatFeatures(
        order: OrderFeatureBridge,
        shipment: ShipmentFeatureBridge,
        taxation: TaxationFeatureBridge
    ) -> OrderBusinessBridge {
        LegacyOrderBusinessBridgeFactory.fromOwnedWrappers(
            order: order,
            shipment: OrderShipmentBridge(feature: shipment),
            taxation: OrderTaxationBridge(feature: taxation)
        )
    }
}
