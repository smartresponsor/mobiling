package app.mobiling.client.contract.order.listing

/**
 * Marketing America Corp. Oleksandr Tishchenko
 */
data class OrderListQuery(
    val stateCode: String?,
    val searchText: String?,
    val page: Int,
    val pageSize: Int,
)
