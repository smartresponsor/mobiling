package com.smartresponsor.mobile.client.data.order.listing

import com.smartresponsor.mobile.client.contract.order.listing.listordersquery
import com.smartresponsor.mobile.client.contract.order.listing.ordersummary

/**
 * Marketing America Corp. Oleksandr Tishchenko
 */
interface OrderListingGateway {
    suspend fun listOrders(query: ListOrdersQuery): List<OrderSummary>
}
