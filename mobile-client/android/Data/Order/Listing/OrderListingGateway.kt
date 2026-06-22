package app.mobiling.client.data.order.listing

import app.mobiling.client.contract.order.listing.ListOrdersQuery
import app.mobiling.client.contract.order.listing.OrderSummary

/**
 * Marketing America Corp. Oleksandr Tishchenko
 */
interface OrderListingGateway {
    suspend fun listOrders(query: ListOrdersQuery): List<OrderSummary>
}
