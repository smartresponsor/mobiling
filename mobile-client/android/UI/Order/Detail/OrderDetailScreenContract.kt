package app.mobiling.client.client.ui.order.detail

import app.mobiling.client.client.contract.order.detail.OrderDetailPayload

/**
 * Marketing America Corp. Oleksandr Tishchenko
 */
data class OrderDetailScreenState(
    val isLoading: Boolean,
    val payload: OrderDetailPayload?,
)
