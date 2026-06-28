package app.mobiling.client.usecase.taxation.summary

import app.mobiling.client.contract.taxation.summary.TaxLoadSummaryQuery
import app.mobiling.client.contract.taxation.summary.TaxSummary
import app.mobiling.client.data.taxation.summary.TaxSummaryGateway

/**
 * Marketing America Corp. Oleksandr Tishchenko
 */
class TaxLoadSummaryUseCase(
    private val gateway: TaxSummaryGateway,
) {
    suspend operator fun invoke(query: TaxLoadSummaryQuery): TaxSummary = gateway.loadTaxSummary(query)
}
