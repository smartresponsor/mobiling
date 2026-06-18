package com.smartresponsor.mobile.client.ui.order.listing

import com.smartresponsor.mobile.client.contract.order.listing.ordersummary

/**
 * Marketing America Corp. Oleksandr Tishchenko
 */
data class OrderListingScreenState(
    val isLoading: Boolean,
    val items: List<OrderSummary>,
    val activeStateCode: String?,
)
