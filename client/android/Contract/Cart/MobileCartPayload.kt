package app.mobiling.client.contract.cart

/**
 * Marketing America Corp. Oleksandr Tishchenko
 */
data class MobileCartPayload(
    val cartId: String?,
    val cartToken: String,
    val ownerReference: String?,
    val status: String,
    val currencyCode: String,
    val itemCount: Int,
    val subtotalMinor: Long,
    val totalMinor: Long,
    val items: List<CartItemPayload>,
    val expiresAt: String?,
    val updatedAt: String?,
)
