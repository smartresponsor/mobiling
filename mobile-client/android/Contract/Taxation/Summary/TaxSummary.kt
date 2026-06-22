package app.mobiling.client.client.contract.taxation.summary

/**
 * Marketing America Corp. Oleksandr Tishchenko
 */
data class TaxSummary(
    val jurisdictionCode: String,
    val taxableAmountLabel: String,
    val taxAmountLabel: String,
    val totalAmountLabel: String,
    val currencyCode: String,
)
