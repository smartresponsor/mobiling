package com.smartresponsor.mobile.client.contract.order.listing

/**
 * Marketing America Corp. Oleksandr Tishchenko
 */
data class ListOrdersQuery(
    val stateCode: String?,
    val searchText: String?,
    val page: Int,
    val pageSize: Int,
)
