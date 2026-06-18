package com.smartresponsor.mobile.app.order.legacy

import com.smartresponsor.mobile.app.order.OrderBusinessBridge
import com.smartresponsor.mobile.app.order.OrderFeatureBridge
import com.smartresponsor.mobile.app.order.OrderShipmentBridge
import com.smartresponsor.mobile.app.order.OrderTaxationBridge
import com.smartresponsor.mobile.app.shipment.ShipmentFeatureBridge
import com.smartresponsor.mobile.app.taxation.TaxationFeatureBridge

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
