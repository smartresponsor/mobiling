package com.smartresponsor.mobile.client.usecase.order.listing

import com.smartresponsor.mobile.client.contract.order.listing.ListOrdersQuery
import com.smartresponsor.mobile.client.contract.order.listing.OrderSummary
import com.smartresponsor.mobile.client.data.order.listing.OrderListingGateway

/**
 * Marketing America Corp. Oleksandr Tishchenko
 */
class ListOrdersUseCase(
    private val gateway: OrderListingGateway,
) {
    suspend operator fun invoke(query: ListOrdersQuery): List<OrderSummary> = gateway.listOrders(query)
}
