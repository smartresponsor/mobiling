package app.mobiling.client.order

import app.mobiling.client.taxation.TaxationFeatureBridge
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
 * Order-owned taxation bridge.
 *
 * Active surface is gateway-first so the order-owned wrapper no longer depends on
 * the flat TaxationFeatureBridge for runtime behavior. A compatibility constructor
 * remains for transitional call sites.
 */
class OrderTaxationBridge(
    private val summaryGateway: TaxSummaryGateway,
    private val detailGateway: TaxDetailGateway,
) {
    constructor(feature: TaxationFeatureBridge) : this(
        summaryGateway = feature.summaryGateway(),
        detailGateway = feature.detailGateway(),
    )

    suspend fun summary(query: TaxLoadSummaryQuery): TaxSummary =
        TaxLoadSummaryUseCase(summaryGateway).invoke(query)

    suspend fun detail(taxationId: String): TaxDetailPayload =
        TaxLoadDetailUseCase(detailGateway).invoke(taxationId)
}
