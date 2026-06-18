package com.smartresponsor.mobile.client.data.shipment.tracking

import com.smartresponsor.mobile.client.contract.shipment.detail.shipmentdetailpayload
import com.smartresponsor.mobile.client.contract.shipment.tracking.listshipmentsquery
import com.smartresponsor.mobile.client.contract.shipment.tracking.shipmenttrackingsummary

/**
 * Marketing America Corp. Oleksandr Tishchenko
 */
interface ShipmentTrackingGateway {
    suspend fun listShipments(query: ListShipmentsQuery): List<ShipmentTrackingSummary>

    suspend fun loadShipmentDetail(shipmentId: String): ShipmentDetailPayload
}
