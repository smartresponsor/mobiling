package app.mobiling.client.client.data.taxation.summary

import app.mobiling.client.client.contract.taxation.summary.LoadTaxSummaryQuery
import app.mobiling.client.client.contract.taxation.summary.TaxSummary

/**
 * Marketing America Corp. Oleksandr Tishchenko
 */
interface TaxSummaryGateway {
    suspend fun loadTaxSummary(query: LoadTaxSummaryQuery): TaxSummary
}
