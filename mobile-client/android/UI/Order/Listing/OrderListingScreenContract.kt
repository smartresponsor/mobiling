package app.mobiling.client.client.ui.order.listing

import app.mobiling.client.client.contract.order.listing.OrderSummary

/**
 * Marketing America Corp. Oleksandr Tishchenko
 */
data class OrderListingScreenState(
    val isLoading: Boolean,
    val items: List<OrderSummary>,
    val activeStateCode: String?,
)
