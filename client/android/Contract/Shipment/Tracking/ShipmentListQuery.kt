package app.mobiling.client.contract.shipment.tracking

/**
 * Marketing America Corp. Oleksandr Tishchenko
 */
data class ShipmentListQuery(
    val orderId: String?,
    val statusCode: String?,
    val cursor: String?,
    val limit: Int,
)
