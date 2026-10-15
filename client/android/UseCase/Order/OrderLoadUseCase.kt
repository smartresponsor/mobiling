package app.mobiling.client.usecase.order

import app.mobiling.client.contract.order.OrderMobileItemPayload
import app.mobiling.client.data.order.OrderGateway

class OrderLoadUseCase(private val gateway: OrderGateway) { suspend fun load(vendorId: String): List<OrderMobileItemPayload> = gateway.loadOrders(vendorId) }
