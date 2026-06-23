package app.mobiling.client.shipment

import app.mobiling.client.contract.shipment.detail.ShipmentDetailPayload
import app.mobiling.client.contract.shipment.tracking.ListShipmentsQuery
import app.mobiling.client.contract.shipment.tracking.ShipmentTrackingSummary
import app.mobiling.client.data.shipment.tracking.ShipmentTrackingGateway
import app.mobiling.client.usecase.shipment.detail.LoadShipmentDetailUseCase
import app.mobiling.client.usecase.shipment.tracking.ListShipmentsUseCase

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
