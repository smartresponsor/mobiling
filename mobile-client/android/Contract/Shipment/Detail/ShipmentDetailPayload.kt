package app.mobiling.client.client.contract.shipment.detail

/**
 * Marketing America Corp. Oleksandr Tishchenko
 */
data class ShipmentDetailPayload(
    val shipmentId: String,
    val shipmentNumber: String,
    val orderId: String,
    val carrierCode: String,
    val trackingNumber: String,
    val currentStatusCode: String,
    val latestEventLabel: String,
    val destinationLabel: String,
)
