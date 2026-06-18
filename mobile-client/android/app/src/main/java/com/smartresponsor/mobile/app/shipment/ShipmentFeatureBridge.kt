package com.smartresponsor.mobile.app.shipment

import com.smartresponsor.mobile.client.contract.shipment.detail.ShipmentDetailPayload
import com.smartresponsor.mobile.client.contract.shipment.tracking.ListShipmentsQuery
import com.smartresponsor.mobile.client.contract.shipment.tracking.ShipmentTrackingSummary
import com.smartresponsor.mobile.client.data.shipment.tracking.ShipmentTrackingGateway
import com.smartresponsor.mobile.client.usecase.shipment.detail.LoadShipmentDetailUseCase
import com.smartresponsor.mobile.client.usecase.shipment.tracking.ListShipmentsUseCase

/**
 * Marketing America Corp. Oleksandr Tishchenko
 *
 * App-level bridge for shipment-domain controlled rewire.
 * Shipment remains order-owned and should be reached through order navigation.
 */
@Deprecated(
    message = "Prefer OrderBusinessBridge.shipment() or OrderShipmentBridge for order-owned navigation.",
    replaceWith = ReplaceWith("orderBusinessBridge.shipment()")
)
class ShipmentFeatureBridge(
    private val trackingGateway: ShipmentTrackingGateway,
) {
    suspend fun list(query: ListShipmentsQuery): List<ShipmentTrackingSummary> =
        ListShipmentsUseCase(trackingGateway).invoke(query)

    suspend fun detail(shipmentId: String): ShipmentDetailPayload =
        LoadShipmentDetailUseCase(trackingGateway).invoke(shipmentId)

    fun gateway(): ShipmentTrackingGateway = trackingGateway
}
