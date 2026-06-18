package com.smartresponsor.mobile.client.usecase.taxation.summary

import com.smartresponsor.mobile.client.contract.taxation.summary.LoadTaxSummaryQuery
import com.smartresponsor.mobile.client.contract.taxation.summary.TaxSummary
import com.smartresponsor.mobile.client.data.taxation.summary.TaxSummaryGateway

/**
 * Marketing America Corp. Oleksandr Tishchenko
 */
class LoadTaxSummaryUseCase(
    private val gateway: TaxSummaryGateway,
) {
    suspend operator fun invoke(query: LoadTaxSummaryQuery): TaxSummary = gateway.loadTaxSummary(query)
}
