package app.mobiling.client.ui.order.detail

import app.mobiling.client.contract.order.detail.OrderDetailPayload

/**
 * Marketing America Corp. Oleksandr Tishchenko
 */
data class OrderDetailScreenState(
    val isLoading: Boolean,
    val payload: OrderDetailPayload?,
)
