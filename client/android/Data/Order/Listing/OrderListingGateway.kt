package app.mobiling.client.data.order.listing

import app.mobiling.client.contract.order.listing.OrderListQuery
import app.mobiling.client.contract.order.listing.OrderSummary

/**
 * Marketing America Corp. Oleksandr Tishchenko
 */
interface OrderListingGateway {
    suspend fun listOrders(query: OrderListQuery): List<OrderSummary>
}
