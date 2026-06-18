package com.smartresponsor.mobile.client.data.taxation.summary

import com.smartresponsor.mobile.client.contract.taxation.summary.LoadTaxSummaryQuery
import com.smartresponsor.mobile.client.contract.taxation.summary.TaxSummary

/**
 * Marketing America Corp. Oleksandr Tishchenko
 */
interface TaxSummaryGateway {
    suspend fun loadTaxSummary(query: LoadTaxSummaryQuery): TaxSummary
}
