package com.smartresponsor.mobile.client.ui.order.detail

import com.smartresponsor.mobile.client.contract.order.detail.orderdetailpayload

/**
 * Marketing America Corp. Oleksandr Tishchenko
 */
data class OrderDetailScreenState(
    val isLoading: Boolean,
    val payload: OrderDetailPayload?,
)
