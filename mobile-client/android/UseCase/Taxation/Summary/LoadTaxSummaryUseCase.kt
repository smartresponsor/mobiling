package app.mobiling.client.client.usecase.taxation.summary

import app.mobiling.client.client.contract.taxation.summary.LoadTaxSummaryQuery
import app.mobiling.client.client.contract.taxation.summary.TaxSummary
import app.mobiling.client.client.data.taxation.summary.TaxSummaryGateway

/**
 * Marketing America Corp. Oleksandr Tishchenko
 */
class LoadTaxSummaryUseCase(
    private val gateway: TaxSummaryGateway,
) {
    suspend operator fun invoke(query: LoadTaxSummaryQuery): TaxSummary = gateway.loadTaxSummary(query)
}
