package com.smartresponsor.mobile.app.order

/**
 * Marketing America Corp. Oleksandr Tishchenko
 *
 * Canonical route ownership map for order-owned flows.
 */
class OrderOwnedRouteMap {
    fun ownedFlows(): List<OrderOwnedFlow> = listOf(
        OrderOwnedFlow.SHIPMENT,
        OrderOwnedFlow.TAXATION,
    )
}
