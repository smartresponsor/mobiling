package app.mobiling.client.usecase.shipment.tracking

import app.mobiling.client.contract.shipment.tracking.ShipmentListQuery
import app.mobiling.client.contract.shipment.tracking.ShipmentTrackingSummary
import app.mobiling.client.data.shipment.tracking.ShipmentTrackingGateway

/**
 * Marketing America Corp. Oleksandr Tishchenko
 */
class ShipmentListUseCase(
    private val shipmentTrackingGateway: ShipmentTrackingGateway,
) {
    suspend operator fun invoke(query: ShipmentListQuery): List<ShipmentTrackingSummary> =
        shipmentTrackingGateway.listShipments(query)
}
