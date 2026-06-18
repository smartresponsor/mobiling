package com.smartresponsor.mobile.app.order
class OrderOwnedRouteMap {
    fun ownedFlows(): List<OrderOwnedFlow> = listOf(OrderOwnedFlow.SHIPMENT)
    fun embeddedCapabilities(): List<OrderEmbeddedCapability> = listOf(OrderEmbeddedCapability.TAXATION)
}
