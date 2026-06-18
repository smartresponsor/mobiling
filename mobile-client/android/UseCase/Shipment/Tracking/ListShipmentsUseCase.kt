package com.smartresponsor.mobile.client.usecase.shipment.tracking

import com.smartresponsor.mobile.client.contract.shipment.tracking.listshipmentsquery
import com.smartresponsor.mobile.client.contract.shipment.tracking.shipmenttrackingsummary
import com.smartresponsor.mobile.client.data.shipment.tracking.shipmenttrackinggateway

/**
 * Marketing America Corp. Oleksandr Tishchenko
 */
class ListShipmentsUseCase(
    private val shipmentTrackingGateway: ShipmentTrackingGateway,
) {
    suspend operator fun invoke(query: ListShipmentsQuery): List<ShipmentTrackingSummary> =
        shipmentTrackingGateway.listShipments(query)
}
