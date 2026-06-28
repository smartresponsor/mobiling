package app.mobiling.client.contract.cart

/**
 * Marketing America Corp. Oleksandr Tishchenko
 */
data class CartAddItemRequest(
    val offerReference: String,
    val quantity: Int,
    val title: String? = null,
    val unitPriceMinor: Long? = null,
    val currencyCode: String? = null,
)
