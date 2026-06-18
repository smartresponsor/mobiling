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
 * Transitional order compatibility boundary.
 *
 * Active compatibility prefers owned wrappers. Flat Shipment/Taxation
 * compatibility is isolated behind LegacyOrderFlatCompatibility so this
 * factory can converge on wrapper-first construction.
 */
object LegacyOrderBusinessBridgeFactory {
    fun fromOwnedWrappers(
        order: OrderFeatureBridge,
        shipment: OrderShipmentBridge,
        taxation: OrderTaxationBridge,
    ): OrderBusinessBridge = OrderBusinessBridge(
        order = order,
        shipment = shipment,
        taxation = taxation,
    )

    @Deprecated(
        message = "Prefer LegacyOrderFlatCompatibility.fromFlatFeatures(...) or fromOwnedWrappers(...).",
        replaceWith = ReplaceWith(
            "LegacyOrderFlatCompatibility.fromFlatFeatures(order, shipment, taxation)"
        )
    )
    fun fromFlatFeatures(
        order: OrderFeatureBridge,
        shipment: ShipmentFeatureBridge,
        taxation: TaxationFeatureBridge,
    ): OrderBusinessBridge = LegacyOrderFlatCompatibility.fromFlatFeatures(
        order = order,
        shipment = shipment,
        taxation = taxation,
    )
}
