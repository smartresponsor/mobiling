package app.mobiling.client.client.usecase.shipment.detail

import app.mobiling.client.client.contract.shipment.detail.ShipmentDetailPayload
import app.mobiling.client.client.data.shipment.tracking.ShipmentTrackingGateway

/**
 * Marketing America Corp. Oleksandr Tishchenko
 */
class LoadShipmentDetailUseCase(
    private val shipmentTrackingGateway: ShipmentTrackingGateway,
) {
    suspend operator fun invoke(shipmentId: String): ShipmentDetailPayload =
        shipmentTrackingGateway.loadShipmentDetail(shipmentId)
}
