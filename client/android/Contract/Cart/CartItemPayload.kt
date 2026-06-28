package app.mobiling.client.contract.cart

/**
 * Marketing America Corp. Oleksandr Tishchenko
 */
data class CartItemPayload(
    val itemId: String,
    val offerReference: String,
    val title: String,
    val unitPriceMinor: Long,
    val currencyCode: String,
    val quantity: Int,
    val lineTotalMinor: Long,
)
