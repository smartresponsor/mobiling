package com.smartresponsor.mobile.client.ui.taxation.summary

import com.smartresponsor.mobile.client.contract.taxation.summary.taxsummary

/**
 * Marketing America Corp. Oleksandr Tishchenko
 */
data class TaxSummaryScreenState(
    val isLoading: Boolean,
    val summary: TaxSummary?,
    val selectedJurisdictionCode: String?,
)
