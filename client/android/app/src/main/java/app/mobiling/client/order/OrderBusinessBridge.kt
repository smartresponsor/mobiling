package app.mobiling.client.order

/**
 * Marketing America Corp. Oleksandr Tishchenko
 *
 * Order-centric business bridge.
 *
 * Active surface is wrapper-first: Order owns Shipment / Taxation through
 * order-owned wrappers only. Legacy flat feature compatibility lives at the
 * wrapper level and no longer contaminates the active business bridge surface.
 */
class OrderBusinessBridge(
    private val order: OrderFeatureBridge,
    private val shipment: OrderShipmentBridge,
    private val taxation: OrderTaxationBridge,
) {
    fun order(): OrderFeatureBridge = order

    fun shipment(): OrderShipmentBridge = shipment

    fun taxation(): OrderTaxationBridge = taxation

    fun ownedRouteMap(): OrderOwnedRouteMap = OrderOwnedRouteMap()
}
