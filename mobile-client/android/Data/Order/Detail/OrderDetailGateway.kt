package app.mobiling.client.client.data.order.detail

import app.mobiling.client.client.contract.order.detail.OrderDetailPayload

/**
 * Marketing America Corp. Oleksandr Tishchenko
 */
interface OrderDetailGateway {
    suspend fun loadOrderDetail(orderId: String): OrderDetailPayload
}
