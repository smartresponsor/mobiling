package app.mobiling.client.contract.taxation.summary

/**
 * Marketing America Corp. Oleksandr Tishchenko
 */
data class TaxLoadSummaryQuery(
    val orderId: String?,
    val shipmentId: String?,
    val productId: String?,
    val jurisdictionCode: String?,
)
