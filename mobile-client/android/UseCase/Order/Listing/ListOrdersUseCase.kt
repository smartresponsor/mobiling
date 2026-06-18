package com.smartresponsor.mobile.client.usecase.order.listing

import com.smartresponsor.mobile.client.contract.order.listing.listordersquery
import com.smartresponsor.mobile.client.contract.order.listing.ordersummary
import com.smartresponsor.mobile.client.data.order.listing.orderlistinggateway

/**
 * Marketing America Corp. Oleksandr Tishchenko
 */
class ListOrdersUseCase(
    private val gateway: OrderListingGateway,
) {
    suspend operator fun invoke(query: ListOrdersQuery): List<OrderSummary> = gateway.listOrders(query)
}
