package com.smartresponsor.mobile.app.order

import com.smartresponsor.mobile.app.shipment.ShipmentFeatureBridge
import com.smartresponsor.mobile.client.contract.shipment.detail.ShipmentDetailPayload
import com.smartresponsor.mobile.client.contract.shipment.tracking.ListShipmentsQuery
import com.smartresponsor.mobile.client.contract.shipment.tracking.ShipmentTrackingSummary
import com.smartresponsor.mobile.client.data.shipment.tracking.ShipmentTrackingGateway
import com.smartresponsor.mobile.client.usecase.shipment.detail.LoadShipmentDetailUseCase
import com.smartresponsor.mobile.client.usecase.shipment.tracking.ListShipmentsUseCase

/**
 * Marketing America Corp. Oleksandr Tishchenko
 *
 * Order-owned shipment bridge.
 *
 * Active surface is gateway-first so the order-owned wrapper no longer depends on
 * the flat ShipmentFeatureBridge for runtime behavior. A compatibility constructor
 * remains for transitional call sites.
 */
class OrderShipmentBridge(
    private val gateway: ShipmentTrackingGateway,
) {
    constructor(feature: ShipmentFeatureBridge) : this(feature.gateway())

    suspend fun list(query: ListShipmentsQuery): List<ShipmentTrackingSummary> =
        ListShipmentsUseCase(gateway).invoke(query)

    suspend fun detail(shipmentId: String): ShipmentDetailPayload =
        LoadShipmentDetailUseCase(gateway).invoke(shipmentId)
}
