package com.smartresponsor.mobile.client.contract.shipment.tracking

/**
 * Marketing America Corp. Oleksandr Tishchenko
 */
data class ListShipmentsQuery(
    val orderId: String?,
    val statusCode: String?,
    val cursor: String?,
    val limit: Int,
)
