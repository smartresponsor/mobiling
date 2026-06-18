package com.smartresponsor.mobile.client.contract.order.listing

/**
 * Marketing America Corp. Oleksandr Tishchenko
 */
data class OrderSummary(
    val orderId: String,
    val orderNumber: String,
    val stateCode: String,
    val totalLabel: String,
    val placedAtIso: String,
    val itemCount: Int,
)
