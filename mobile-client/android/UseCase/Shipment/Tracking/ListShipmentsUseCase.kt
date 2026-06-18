package com.smartresponsor.mobile.client.usecase.shipment.tracking

import com.smartresponsor.mobile.client.contract.shipment.tracking.ListShipmentsQuery
import com.smartresponsor.mobile.client.contract.shipment.tracking.ShipmentTrackingSummary
import com.smartresponsor.mobile.client.data.shipment.tracking.ShipmentTrackingGateway

/**
 * Marketing America Corp. Oleksandr Tishchenko
 */
class ListShipmentsUseCase(
    private val shipmentTrackingGateway: ShipmentTrackingGateway,
) {
    suspend operator fun invoke(query: ListShipmentsQuery): List<ShipmentTrackingSummary> =
        shipmentTrackingGateway.listShipments(query)
}
