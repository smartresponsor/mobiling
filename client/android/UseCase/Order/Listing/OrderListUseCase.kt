package app.mobiling.client.usecase.order.listing

import app.mobiling.client.contract.order.listing.OrderListQuery
import app.mobiling.client.contract.order.listing.OrderSummary
import app.mobiling.client.data.order.listing.OrderListingGateway

/**
 * Marketing America Corp. Oleksandr Tishchenko
 */
class OrderListUseCase(
    private val gateway: OrderListingGateway,
) {
    suspend operator fun invoke(query: OrderListQuery): List<OrderSummary> = gateway.listOrders(query)
}
