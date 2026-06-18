package com.smartresponsor.mobile.client.usecase.taxation.summary

import com.smartresponsor.mobile.client.contract.taxation.summary.loadtaxsummaryquery
import com.smartresponsor.mobile.client.contract.taxation.summary.taxsummary
import com.smartresponsor.mobile.client.data.taxation.summary.taxsummarygateway

/**
 * Marketing America Corp. Oleksandr Tishchenko
 */
class LoadTaxSummaryUseCase(
    private val gateway: TaxSummaryGateway,
) {
    suspend operator fun invoke(query: LoadTaxSummaryQuery): TaxSummary = gateway.loadTaxSummary(query)
}
