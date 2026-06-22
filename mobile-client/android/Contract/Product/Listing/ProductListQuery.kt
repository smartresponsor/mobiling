package app.mobiling.client.contract.product.listing

/**
 * Marketing America Corp. Oleksandr Tishchenko
 */
data class ProductListQuery(
    val searchTerm: String?,
    val page: Int,
    val pageSize: Int,
)
