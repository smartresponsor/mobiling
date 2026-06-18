package com.smartresponsor.mobile.app.vendor

import com.smartresponsor.mobile.app.order.OrderBusinessBridge
import com.smartresponsor.mobile.app.order.OrderOwnedFlow
import com.smartresponsor.mobile.app.order.OrderOwnedRouteMap
import com.smartresponsor.mobile.client.contract.order.detail.OrderDetailPayload
import com.smartresponsor.mobile.client.contract.order.listing.ListOrdersQuery
import com.smartresponsor.mobile.client.contract.order.listing.OrderSummary

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
