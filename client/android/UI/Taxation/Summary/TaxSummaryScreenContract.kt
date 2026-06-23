package app.mobiling.client.ui.taxation.summary

import app.mobiling.client.contract.taxation.summary.TaxSummary

/**
 * Marketing America Corp. Oleksandr Tishchenko
 */
data class TaxSummaryScreenState(
    val isLoading: Boolean,
    val summary: TaxSummary?,
    val selectedJurisdictionCode: String?,
)
