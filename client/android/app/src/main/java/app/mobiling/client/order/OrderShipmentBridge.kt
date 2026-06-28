package app.mobiling.client.order

import app.mobiling.client.shipment.ShipmentFeatureBridge
import app.mobiling.client.contract.shipment.detail.ShipmentDetailPayload
import app.mobiling.client.contract.shipment.tracking.ShipmentListQuery
import app.mobiling.client.contract.shipment.tracking.ShipmentTrackingSummary
import app.mobiling.client.data.shipment.tracking.ShipmentTrackingGateway
import app.mobiling.client.usecase.shipment.detail.ShipmentLoadDetailUseCase
import app.mobiling.client.usecase.shipment.tracking.ShipmentListUseCase

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

    suspend fun list(query: ShipmentListQuery): List<ShipmentTrackingSummary> =
        ShipmentListUseCase(gateway).invoke(query)

    suspend fun detail(shipmentId: String): ShipmentDetailPayload =
        ShipmentLoadDetailUseCase(gateway).invoke(shipmentId)
}
