package app.mobiling.client.client.usecase.order.listing

import app.mobiling.client.client.contract.order.listing.ListOrdersQuery
import app.mobiling.client.client.contract.order.listing.OrderSummary
import app.mobiling.client.client.data.order.listing.OrderListingGateway

/**
 * Marketing America Corp. Oleksandr Tishchenko
 */
class ListOrdersUseCase(
    private val gateway: OrderListingGateway,
) {
    suspend operator fun invoke(query: ListOrdersQuery): List<OrderSummary> = gateway.listOrders(query)
}
