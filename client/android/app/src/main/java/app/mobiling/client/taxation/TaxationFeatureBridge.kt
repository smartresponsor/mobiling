package app.mobiling.client.taxation

import app.mobiling.client.contract.taxation.detail.TaxDetailPayload
import app.mobiling.client.contract.taxation.summary.TaxLoadSummaryQuery
import app.mobiling.client.contract.taxation.summary.TaxSummary
import app.mobiling.client.data.taxation.detail.TaxDetailGateway
import app.mobiling.client.data.taxation.summary.TaxSummaryGateway
import app.mobiling.client.usecase.taxation.detail.TaxLoadDetailUseCase
import app.mobiling.client.usecase.taxation.summary.TaxLoadSummaryUseCase

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
    suspend fun summary(query: TaxLoadSummaryQuery): TaxSummary =
        TaxLoadSummaryUseCase(summaryGateway).invoke(query)

    suspend fun detail(taxationId: String): TaxDetailPayload =
        TaxLoadDetailUseCase(detailGateway).invoke(taxationId)

    fun summaryGateway(): TaxSummaryGateway = summaryGateway

    fun detailGateway(): TaxDetailGateway = detailGateway
}
