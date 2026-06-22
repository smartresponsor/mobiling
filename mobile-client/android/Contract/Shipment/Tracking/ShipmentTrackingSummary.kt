package app.mobiling.client.client.contract.shipment.tracking

/**
 * Marketing America Corp. Oleksandr Tishchenko
 */
data class ShipmentTrackingSummary(
    val shipmentId: String,
    val shipmentNumber: String,
    val carrierCode: String,
    val trackingNumber: String,
    val currentStatusCode: String,
    val updatedAtIso: String,
)
