package app.mobiling.client.client.contract.order.detail

/**
 * Marketing America Corp. Oleksandr Tishchenko
 */
data class OrderDetailPayload(
    val orderId: String,
    val orderNumber: String,
    val stateCode: String,
    val totalLabel: String,
    val subtotalLabel: String,
    val taxationLabel: String?,
    val shippingLabel: String?,
    val placedAtIso: String,
    val lineItems: List<OrderLineItemPayload>,
)

data class OrderLineItemPayload(
    val lineId: String,
    val productId: String,
    val productTitle: String,
    val quantity: Int,
    val lineTotalLabel: String,
)
