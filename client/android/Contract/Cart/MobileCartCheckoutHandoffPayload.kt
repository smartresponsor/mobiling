package app.mobiling.client.contract.cart

/**
 * Marketing America Corp. Oleksandr Tishchenko
 */
data class MobileCartCheckoutHandoffPayload(
    val cartId: String?,
    val cartToken: String,
    val handoffId: String,
    val checkoutUrl: String?,
    val status: String,
    val expiresAt: String?,
)
