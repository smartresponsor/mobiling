package com.smartresponsor.mobile.app.order

import com.smartresponsor.mobile.app.taxation.TaxationFeatureBridge
import com.smartresponsor.mobile.client.contract.taxation.detail.TaxDetailPayload
import com.smartresponsor.mobile.client.contract.taxation.summary.LoadTaxSummaryQuery
import com.smartresponsor.mobile.client.contract.taxation.summary.TaxSummary
import com.smartresponsor.mobile.client.data.taxation.detail.TaxDetailGateway
import com.smartresponsor.mobile.client.data.taxation.summary.TaxSummaryGateway
import com.smartresponsor.mobile.client.usecase.taxation.detail.LoadTaxDetailUseCase
import com.smartresponsor.mobile.client.usecase.taxation.summary.LoadTaxSummaryUseCase

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
