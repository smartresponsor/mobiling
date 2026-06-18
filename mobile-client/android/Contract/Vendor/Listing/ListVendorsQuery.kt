package com.smartresponsor.mobile.client.contract.vendor.listing

/**
 * Marketing America Corp. Oleksandr Tishchenko
 */
data class ListVendorsQuery(
    val searchText: String?,
    val categoryCode: String?,
    val cityCode: String?,
    val page: Int,
    val pageSize: Int,
)
