package com.smartresponsor.mobile.client.data.shipment.tracking

import com.smartresponsor.mobile.client.contract.shipment.detail.ShipmentDetailPayload
import com.smartresponsor.mobile.client.contract.shipment.tracking.ListShipmentsQuery
import com.smartresponsor.mobile.client.contract.shipment.tracking.ShipmentTrackingSummary

/**
 * Marketing America Corp. Oleksandr Tishchenko
 */
interface ShipmentTrackingGateway {
    suspend fun listShipments(query: ListShipmentsQuery): List<ShipmentTrackingSummary>

    suspend fun loadShipmentDetail(shipmentId: String): ShipmentDetailPayload
}
