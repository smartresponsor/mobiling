package app.mobiling.client.vendor

import app.mobiling.client.order.OrderBusinessBridge
import app.mobiling.client.order.OrderOwnedFlow
import app.mobiling.client.order.OrderOwnedRouteMap
import app.mobiling.client.contract.order.detail.OrderDetailPayload
import app.mobiling.client.contract.order.listing.ListOrdersQuery
import app.mobiling.client.contract.order.listing.OrderSummary

/**
 * Marketing America Corp. Oleksandr Tishchenko
 *
 * Vendor-owned order bridge.
 */
class VendorOrderBridge(
    private val business: OrderBusinessBridge,
) {
    suspend fun list(query: ListOrdersQuery): List<OrderSummary> = business.order().list(query)

    suspend fun detail(orderId: String): OrderDetailPayload = business.order().detail(orderId)

    fun ownership(): OrderBusinessBridge = business

    fun ownedFlows(): List<OrderOwnedFlow> = OrderOwnedRouteMap().ownedFlows()
}
