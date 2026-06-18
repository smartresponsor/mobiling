package com.smartresponsor.mobile.client.usecase.shipment.detail

import com.smartresponsor.mobile.client.contract.shipment.detail.ShipmentDetailPayload
import com.smartresponsor.mobile.client.data.shipment.tracking.ShipmentTrackingGateway

/**
 * Marketing America Corp. Oleksandr Tishchenko
 */
class LoadShipmentDetailUseCase(
    private val shipmentTrackingGateway: ShipmentTrackingGateway,
) {
    suspend operator fun invoke(shipmentId: String): ShipmentDetailPayload =
        shipmentTrackingGateway.loadShipmentDetail(shipmentId)
}
