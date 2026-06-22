package app.mobiling.client.usecase.shipment.tracking

import app.mobiling.client.contract.shipment.tracking.ListShipmentsQuery
import app.mobiling.client.contract.shipment.tracking.ShipmentTrackingSummary
import app.mobiling.client.data.shipment.tracking.ShipmentTrackingGateway

/**
 * Marketing America Corp. Oleksandr Tishchenko
 */
class ListShipmentsUseCase(
    private val shipmentTrackingGateway: ShipmentTrackingGateway,
) {
    suspend operator fun invoke(query: ListShipmentsQuery): List<ShipmentTrackingSummary> =
        shipmentTrackingGateway.listShipments(query)
}
