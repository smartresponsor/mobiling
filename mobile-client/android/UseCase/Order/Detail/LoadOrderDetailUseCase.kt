package app.mobiling.client.client.usecase.order.detail

import app.mobiling.client.client.contract.order.detail.OrderDetailPayload
import app.mobiling.client.client.data.order.detail.OrderDetailGateway

/**
 * Marketing America Corp. Oleksandr Tishchenko
 */
class LoadOrderDetailUseCase(
    private val gateway: OrderDetailGateway,
) {
    suspend operator fun invoke(orderId: String): OrderDetailPayload = gateway.loadOrderDetail(orderId)
}
