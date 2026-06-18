package com.smartresponsor.mobile.client.ui.shipment.tracking

import com.smartresponsor.mobile.client.contract.shipment.tracking.shipmenttrackingsummary

/**
 * Marketing America Corp. Oleksandr Tishchenko
 */
data class ShipmentTrackingScreenContract(
    val title: String,
    val rows: List<ShipmentTrackingSummary>,
    val nextCursor: String?,
)
