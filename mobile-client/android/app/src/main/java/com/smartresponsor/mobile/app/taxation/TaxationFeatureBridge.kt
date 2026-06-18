package com.smartresponsor.mobile.app.taxation

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
