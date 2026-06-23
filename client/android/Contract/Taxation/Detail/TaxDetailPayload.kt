package app.mobiling.client.contract.taxation.detail

/**
 * Marketing America Corp. Oleksandr Tishchenko
 */
data class TaxDetailPayload(
    val jurisdictionCode: String,
    val taxableAmountLabel: String,
    val subtotalAmountLabel: String,
    val taxAmountLabel: String,
    val totalAmountLabel: String,
    val rateLabel: String,
    val breakdownLines: List<String>,
)
