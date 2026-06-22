package app.mobiling.client.client.ui.taxation.summary

import app.mobiling.client.client.contract.taxation.summary.TaxSummary

/**
 * Marketing America Corp. Oleksandr Tishchenko
 */
data class TaxSummaryScreenState(
    val isLoading: Boolean,
    val summary: TaxSummary?,
    val selectedJurisdictionCode: String?,
)
