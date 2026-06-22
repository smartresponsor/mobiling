package app.mobiling.client.data.shipment.tracking

import app.mobiling.client.contract.shipment.detail.ShipmentDetailPayload
import app.mobiling.client.contract.shipment.tracking.ListShipmentsQuery
import app.mobiling.client.contract.shipment.tracking.ShipmentTrackingSummary

/**
 * Marketing America Corp. Oleksandr Tishchenko
 */
interface ShipmentTrackingGateway {
    suspend fun listShipments(query: ListShipmentsQuery): List<ShipmentTrackingSummary>

    suspend fun loadShipmentDetail(shipmentId: String): ShipmentDetailPayload
}
