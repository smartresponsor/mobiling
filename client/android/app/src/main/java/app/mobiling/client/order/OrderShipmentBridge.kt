package app.mobiling.client.order

import app.mobiling.client.shipment.ShipmentFeatureBridge
import app.mobiling.client.contract.shipment.detail.ShipmentDetailPayload
import app.mobiling.client.contract.shipment.tracking.ListShipmentsQuery
import app.mobiling.client.contract.shipment.tracking.ShipmentTrackingSummary
import app.mobiling.client.data.shipment.tracking.ShipmentTrackingGateway
import app.mobiling.client.usecase.shipment.detail.LoadShipmentDetailUseCase
import app.mobiling.client.usecase.shipment.tracking.ListShipmentsUseCase

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
