package com.smartresponsor.mobile.client.usecase.order.detail

import com.smartresponsor.mobile.client.contract.order.detail.orderdetailpayload
import com.smartresponsor.mobile.client.data.order.detail.orderdetailgateway

/**
 * Marketing America Corp. Oleksandr Tishchenko
 */
class LoadOrderDetailUseCase(
    private val gateway: OrderDetailGateway,
) {
    suspend operator fun invoke(orderId: String): OrderDetailPayload = gateway.loadOrderDetail(orderId)
}
