package app.mobiling.client.ui.shipment.tracking

import app.mobiling.client.contract.shipment.tracking.ShipmentTrackingSummary

/**
 * Marketing America Corp. Oleksandr Tishchenko
 */
data class ShipmentTrackingScreenContract(
    val title: String,
    val rows: List<ShipmentTrackingSummary>,
    val nextCursor: String?,
)
