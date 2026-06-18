package com.smartresponsor.mobile.client.data.order.listing

import com.smartresponsor.mobile.client.contract.order.listing.ListOrdersQuery
import com.smartresponsor.mobile.client.contract.order.listing.OrderSummary

/**
 * Marketing America Corp. Oleksandr Tishchenko
 */
interface OrderListingGateway {
    suspend fun listOrders(query: ListOrdersQuery): List<OrderSummary>
}
