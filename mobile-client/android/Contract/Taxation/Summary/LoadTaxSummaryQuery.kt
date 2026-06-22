package app.mobiling.client.client.contract.taxation.summary

/**
 * Marketing America Corp. Oleksandr Tishchenko
 */
data class LoadTaxSummaryQuery(
    val orderId: String?,
    val shipmentId: String?,
    val productId: String?,
    val jurisdictionCode: String?,
)
