package app.mobiling.client.data.taxation.summary

import app.mobiling.client.contract.taxation.summary.TaxLoadSummaryQuery
import app.mobiling.client.contract.taxation.summary.TaxSummary

/**
 * Marketing America Corp. Oleksandr Tishchenko
 */
interface TaxSummaryGateway {
    suspend fun loadTaxSummary(query: TaxLoadSummaryQuery): TaxSummary
}
