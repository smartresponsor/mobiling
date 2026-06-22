package app.mobiling.client.taxation

import app.mobiling.client.client.contract.taxation.detail.TaxDetailPayload
import app.mobiling.client.client.contract.taxation.summary.LoadTaxSummaryQuery
import app.mobiling.client.client.contract.taxation.summary.TaxSummary
import app.mobiling.client.client.data.taxation.detail.TaxDetailGateway
import app.mobiling.client.client.data.taxation.summary.TaxSummaryGateway
import app.mobiling.client.client.usecase.taxation.detail.LoadTaxDetailUseCase
import app.mobiling.client.client.usecase.taxation.summary.LoadTaxSummaryUseCase

/**
 * Marketing America Corp. Oleksandr Tishchenko
 *
 * App-level bridge for taxation-domain controlled rewire.
 * Taxation remains embedded in order ownership and should not surface as an independent root.
 */
@Deprecated(
    message = "Prefer OrderBusinessBridge.taxation() or OrderTaxationBridge for order-owned navigation.",
    replaceWith = ReplaceWith("orderBusinessBridge.taxation()")
)
class TaxationFeatureBridge(
    private val summaryGateway: TaxSummaryGateway,
    private val detailGateway: TaxDetailGateway,
) {
    suspend fun summary(query: LoadTaxSummaryQuery): TaxSummary =
        LoadTaxSummaryUseCase(summaryGateway).invoke(query)

    suspend fun detail(taxationId: String): TaxDetailPayload =
        LoadTaxDetailUseCase(detailGateway).invoke(taxationId)

    fun summaryGateway(): TaxSummaryGateway = summaryGateway

    fun detailGateway(): TaxDetailGateway = detailGateway
}
