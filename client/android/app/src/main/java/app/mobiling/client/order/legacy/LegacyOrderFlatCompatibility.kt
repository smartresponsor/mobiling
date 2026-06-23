package app.mobiling.client.order.legacy

import app.mobiling.client.order.OrderBusinessBridge
import app.mobiling.client.order.OrderFeatureBridge
import app.mobiling.client.order.OrderShipmentBridge
import app.mobiling.client.order.OrderTaxationBridge
import app.mobiling.client.shipment.ShipmentFeatureBridge
import app.mobiling.client.taxation.TaxationFeatureBridge

/**
 * Marketing America Corp. Oleksandr Tishchenko
 *
 * Dedicated flat compatibility shim for order-owned subflows.
 *
 * This file is the only place where flat ShipmentFeatureBridge /
 * TaxationFeatureBridge are adapted into order-owned wrappers for
 * compatibility purposes.
 */
object LegacyOrderFlatCompatibility {
    fun fromFlatFeatures(
        order: OrderFeatureBridge,
        shipment: ShipmentFeatureBridge,
        taxation: TaxationFeatureBridge,
    ): OrderBusinessBridge = LegacyOrderBusinessBridgeFactory.fromOwnedWrappers(
        order = order,
        shipment = OrderShipmentBridge(shipment),
        taxation = OrderTaxationBridge(taxation),
    )
}
