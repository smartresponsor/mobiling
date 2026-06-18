package com.smartresponsor.mobile.client.data.order.detail

import com.smartresponsor.mobile.client.contract.order.detail.OrderDetailPayload

/**
 * Marketing America Corp. Oleksandr Tishchenko
 */
interface OrderDetailGateway {
    suspend fun loadOrderDetail(orderId: String): OrderDetailPayload
}
