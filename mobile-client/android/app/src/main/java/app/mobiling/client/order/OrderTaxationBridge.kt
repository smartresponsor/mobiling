package app.mobiling.client.order

import app.mobiling.client.taxation.TaxationFeatureBridge
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

    suspend fun summary(query: LoadTaxSummaryQuery): TaxSummary =
        LoadTaxSummaryUseCase(summaryGateway).invoke(query)

    suspend fun detail(taxationId: String): TaxDetailPayload =
        LoadTaxDetailUseCase(detailGateway).invoke(taxationId)
}
