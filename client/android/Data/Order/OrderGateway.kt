package app.mobiling.client.data.order

import app.mobiling.client.contract.order.OrderMobileItemPayload

interface OrderGateway {
    suspend fun loadOrders(vendorId: String): List<OrderMobileItemPayload>
    suspend fun createOrder(fields: Map<String, String>)
    suspend fun updateOrder(orderId: String, fields: Map<String, String>)
    suspend fun deleteOrder(orderId: String)
}
